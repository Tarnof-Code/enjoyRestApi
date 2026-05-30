package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Chambre;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.TypeChambre;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveChambreRequest;
import com.tarnof.enjoyrestapi.payload.response.ChambreDto;
import com.tarnof.enjoyrestapi.repositories.ChambreRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.ChambreService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class ChambreServiceImpl implements ChambreService {

    private final ChambreRepository chambreRepository;
    private final SejourVerificationService sejourVerificationService;
    private final UtilisateurRepository utilisateurRepository;

    public ChambreServiceImpl(
            ChambreRepository chambreRepository,
            SejourVerificationService sejourVerificationService,
            UtilisateurRepository utilisateurRepository) {
        this.chambreRepository = chambreRepository;
        this.sejourVerificationService = sejourVerificationService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChambreDto> listerChambresDuSejour(int sejourId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        return chambreRepository.findBySejourIdOrderAffichage(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ChambreDto getChambre(int sejourId, int chambreId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        return mapToDto(getChambreEtVerifierSejour(sejourId, chambreId));
    }

    @Override
    @Transactional
    public ChambreDto creerChambre(int sejourId, SaveChambreRequest request) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        String identifiant = normaliserIdentifiant(request.identifiant());
        verifierIdentifiantChambreUniquePourSejour(sejourId, identifiant, null);
        Chambre chambre = new Chambre();
        appliquerRequete(chambre, request, identifiant);
        chambre.setSejour(sejour);
        return mapToDto(chambreRepository.save(chambre));
    }

    @Override
    @Transactional
    public ChambreDto modifierChambre(int sejourId, int chambreId, SaveChambreRequest request) {
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        String identifiant = normaliserIdentifiant(request.identifiant());
        verifierIdentifiantChambreUniquePourSejour(sejourId, identifiant, chambreId);
        appliquerRequete(chambre, request, identifiant);
        return mapToDto(chambreRepository.save(chambre));
    }

    @Override
    @Transactional
    public void supprimerChambre(int sejourId, int chambreId) {
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        chambreRepository.delete(chambre);
    }

    @Override
    @Transactional
    public void ajouterReferent(int sejourId, int chambreId, AjouterReferentRequest request) {
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        verifierChambreAccepteReferents(chambre);
        Utilisateur referent = utilisateurRepository.findByTokenId(request.referentTokenId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Référent non trouvé avec l'ID: " + request.referentTokenId()));
        if (chambre.getReferents().stream().anyMatch(r -> r.getId() == referent.getId())) {
            throw new ResourceAlreadyExistsException("Ce référent fait déjà partie de la chambre");
        }
        chambre.getReferents().add(referent);
        chambreRepository.save(chambre);
    }

    @Override
    @Transactional
    public void retirerReferent(int sejourId, int chambreId, String referentTokenId) {
        Chambre chambre = getChambreEtVerifierSejour(sejourId, chambreId);
        verifierChambreAccepteReferents(chambre);
        Utilisateur referent = utilisateurRepository.findByTokenId(referentTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Référent non trouvé avec l'ID: " + referentTokenId));
        boolean removed = chambre.getReferents().removeIf(r -> r.getId() == referent.getId());
        if (!removed) {
            throw new ResourceNotFoundException("Ce référent ne fait pas partie de la chambre");
        }
        chambreRepository.save(chambre);
    }

    private Chambre getChambreEtVerifierSejour(int sejourId, int chambreId) {
        Chambre chambre = chambreRepository.findById(chambreId)
                .orElseThrow(() -> new ResourceNotFoundException("Chambre non trouvée avec l'ID: " + chambreId));
        if (chambre.getSejour().getId() != sejourId) {
            throw new ResourceNotFoundException("La chambre n'appartient pas à ce séjour");
        }
        return chambre;
    }

    private static void appliquerRequete(Chambre chambre, SaveChambreRequest request, String identifiant) {
        chambre.setTypeChambre(request.typeChambre());
        chambre.setIdentifiant(identifiant);
        chambre.setNom(normaliserTexteOptionnel(request.nom()));
        chambre.setCapaciteMax(request.capaciteMax());
        chambre.setGenreAutorise(request.genreAutorise());
        chambre.setDescription(normaliserTexteOptionnel(request.description()));
        chambre.setBatiment(normaliserTexteOptionnel(request.batiment()));
        chambre.setCouloir(normaliserTexteOptionnel(request.couloir()));
        chambre.setEtage(request.etage());
        if (request.typeChambre() == TypeChambre.EQUIPE) {
            chambre.getReferents().clear();
        }
    }

    private static void verifierChambreAccepteReferents(Chambre chambre) {
        if (chambre.getTypeChambre() == TypeChambre.EQUIPE) {
            throw new IllegalArgumentException(
                    "Les chambres équipe n'acceptent pas de référents.");
        }
    }

    private static String normaliserIdentifiant(String identifiant) {
        return identifiant == null ? "" : identifiant.trim();
    }

    private static String normaliserTexteOptionnel(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void verifierIdentifiantChambreUniquePourSejour(int sejourId, String identifiant, Integer excludeChambreId) {
        if (identifiant.isEmpty()) {
            throw new IllegalArgumentException("L'identifiant de la chambre ne peut pas être vide");
        }
        boolean doublon = excludeChambreId == null
                ? chambreRepository.existsBySejourIdAndIdentifiantIgnoreCase(sejourId, identifiant)
                : chambreRepository.existsBySejourIdAndIdentifiantIgnoreCaseAndIdNot(
                        sejourId, identifiant, excludeChambreId);
        if (doublon) {
            throw new ResourceAlreadyExistsException(
                    "Une chambre avec cet identifiant existe déjà pour ce séjour");
        }
    }

    private ChambreDto mapToDto(Chambre chambre) {
        List<ChambreDto.ReferentInfos> referents =
                chambre.getTypeChambre() == TypeChambre.ENFANT && chambre.getReferents() != null
                        ? chambre.getReferents().stream()
                                .map(r -> new ChambreDto.ReferentInfos(
                                        r.getTokenId(), r.getNom(), r.getPrenom()))
                                .collect(Collectors.toList())
                        : List.of();
        return new ChambreDto(
                chambre.getId(),
                chambre.getSejour().getId(),
                chambre.getTypeChambre(),
                chambre.getIdentifiant(),
                chambre.getNom(),
                chambre.getCapaciteMax(),
                chambre.getGenreAutorise(),
                chambre.getDescription(),
                chambre.getBatiment(),
                chambre.getCouloir(),
                chambre.getEtage(),
                referents);
    }
}
