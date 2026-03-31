package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Lieu;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveLieuRequest;
import com.tarnof.enjoyrestapi.payload.response.LieuDto;
import com.tarnof.enjoyrestapi.repositories.LieuRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.services.LieuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class LieuServiceImpl implements LieuService {

    private final LieuRepository lieuRepository;
    private final SejourRepository sejourRepository;

    public LieuServiceImpl(LieuRepository lieuRepository, SejourRepository sejourRepository) {
        this.lieuRepository = lieuRepository;
        this.sejourRepository = sejourRepository;
    }

    @Override
    public List<LieuDto> listerLieuxDuSejour(int sejourId) {
        verifierSejourExiste(sejourId);
        return lieuRepository.findBySejourId(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public LieuDto getLieu(int sejourId, int lieuId) {
        return mapToDto(getLieuEtVerifierSejour(sejourId, lieuId));
    }

    @Override
    @Transactional
    public LieuDto creerLieu(int sejourId, SaveLieuRequest request) {
        Sejour sejour = verifierSejourExiste(sejourId);
        String nom = normaliserNom(request.nom());
        verifierNomLieuUniquePourSejour(sejourId, nom, null);
        validerParametresPartage(request);
        Lieu lieu = new Lieu();
        lieu.setNom(nom);
        lieu.setEmplacement(request.emplacement());
        lieu.setNombreMax(request.nombreMax());
        lieu.setPartageableEntreAnimateurs(request.partageableEntreAnimateurs());
        lieu.setNombreMaxActivitesSimultanees(
                request.partageableEntreAnimateurs() ? request.nombreMaxActivitesSimultanees() : null);
        lieu.setSejour(sejour);
        return mapToDto(lieuRepository.save(lieu));
    }

    @Override
    @Transactional
    public LieuDto modifierLieu(int sejourId, int lieuId, SaveLieuRequest request) {
        Lieu lieu = getLieuEtVerifierSejour(sejourId, lieuId);
        String nom = normaliserNom(request.nom());
        verifierNomLieuUniquePourSejour(sejourId, nom, lieuId);
        validerParametresPartage(request);
        lieu.setNom(nom);
        lieu.setEmplacement(request.emplacement());
        lieu.setNombreMax(request.nombreMax());
        lieu.setPartageableEntreAnimateurs(request.partageableEntreAnimateurs());
        lieu.setNombreMaxActivitesSimultanees(
                request.partageableEntreAnimateurs() ? request.nombreMaxActivitesSimultanees() : null);
        return mapToDto(lieuRepository.save(lieu));
    }

    @Override
    @Transactional
    public void supprimerLieu(int sejourId, int lieuId) {
        Lieu lieu = getLieuEtVerifierSejour(sejourId, lieuId);
        lieuRepository.delete(lieu);
    }

    private Sejour verifierSejourExiste(int sejourId) {
        return sejourRepository.findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
    }

    private Lieu getLieuEtVerifierSejour(int sejourId, int lieuId) {
        Lieu lieu = lieuRepository.findById(lieuId)
                .orElseThrow(() -> new ResourceNotFoundException("Lieu non trouvé avec l'ID: " + lieuId));
        if (lieu.getSejour().getId() != sejourId) {
            throw new ResourceNotFoundException("Le lieu n'appartient pas à ce séjour");
        }
        return lieu;
    }

    private static String normaliserNom(String nom) {
        return nom == null ? "" : nom.trim();
    }

    /**
     * Un nom de lieu est unique par séjour (comparaison insensible à la casse).
     *
     * @param excludeLieuId id du lieu à exclure lors d'une mise à jour ({@code null} à la création)
     */
    private static void validerParametresPartage(SaveLieuRequest request) {
        if (request.partageableEntreAnimateurs()) {
            Integer max = request.nombreMaxActivitesSimultanees();
            if (max == null || max < 2) {
                throw new IllegalArgumentException(
                        "Lorsque le lieu est partageable entre animateurs, le nombre maximal d’activités "
                                + "simultanées doit être défini et au moins égal à 2.");
            }
        } else if (request.nombreMaxActivitesSimultanees() != null) {
            throw new IllegalArgumentException(
                    "Le nombre maximal d’activités simultanées n’a de sens que si le partage entre animateurs est activé.");
        }
    }

    private void verifierNomLieuUniquePourSejour(int sejourId, String nom, Integer excludeLieuId) {
        boolean doublon = excludeLieuId == null
                ? lieuRepository.existsBySejourIdAndNomIgnoreCase(sejourId, nom)
                : lieuRepository.existsBySejourIdAndNomIgnoreCaseAndIdNot(sejourId, nom, excludeLieuId);
        if (doublon) {
            throw new ResourceAlreadyExistsException("Un lieu avec ce nom existe déjà pour ce séjour");
        }
    }

    private LieuDto mapToDto(Lieu lieu) {
        return new LieuDto(
                lieu.getId(),
                lieu.getNom(),
                lieu.getEmplacement(),
                lieu.getNombreMax(),
                lieu.isPartageableEntreAnimateurs(),
                lieu.getNombreMaxActivitesSimultanees(),
                lieu.getSejour().getId()
        );
    }
}
