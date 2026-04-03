package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Moment;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.ReorderMomentsRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveMomentRequest;
import com.tarnof.enjoyrestapi.payload.response.MomentDto;
import com.tarnof.enjoyrestapi.repositories.ActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.MomentRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.services.MomentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class MomentServiceImpl implements MomentService {

    private final MomentRepository momentRepository;
    private final SejourRepository sejourRepository;
    private final ActiviteRepository activiteRepository;

    public MomentServiceImpl(
            MomentRepository momentRepository,
            SejourRepository sejourRepository,
            ActiviteRepository activiteRepository) {
        this.momentRepository = momentRepository;
        this.sejourRepository = sejourRepository;
        this.activiteRepository = activiteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MomentDto> listerMomentsDuSejour(int sejourId) {
        verifierSejourExiste(sejourId);
        return momentRepository.findBySejourIdOrderChronologique(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MomentDto getMoment(int sejourId, int momentId) {
        return mapToDto(getMomentEtVerifierSejour(sejourId, momentId));
    }

    @Override
    @Transactional
    public MomentDto creerMoment(int sejourId, SaveMomentRequest request) {
        Sejour sejour = verifierSejourExiste(sejourId);
        String nom = normaliserNom(request.nom());
        verifierNomMomentUniquePourSejour(sejourId, nom, null);
        Moment moment = new Moment();
        moment.setNom(nom);
        moment.setSejour(sejour);
        moment.setOrdre(prochainOrdrePourSejour(sejourId));
        return mapToDto(momentRepository.save(moment));
    }

    @Override
    @Transactional
    public MomentDto modifierMoment(int sejourId, int momentId, SaveMomentRequest request) {
        Moment moment = getMomentEtVerifierSejour(sejourId, momentId);
        String nom = normaliserNom(request.nom());
        verifierNomMomentUniquePourSejour(sejourId, nom, momentId);
        moment.setNom(nom);
        return mapToDto(momentRepository.save(moment));
    }

    @Override
    @Transactional
    public List<MomentDto> reorderMoments(int sejourId, ReorderMomentsRequest request) {
        verifierSejourExiste(sejourId);
        List<Moment> existants = momentRepository.findBySejourIdOrderChronologique(sejourId);
        Set<Integer> idsSejour =
                existants.stream().map(Moment::getId).collect(Collectors.toSet());
        List<Integer> demandes = request.momentIds();
        if (demandes.size() != existants.size()) {
            throw new IllegalArgumentException(
                    "La liste doit contenir exactement tous les moments du séjour, dans le nouvel ordre.");
        }
        if (demandes.size() != new HashSet<>(demandes).size()) {
            throw new IllegalArgumentException("La liste des identifiants ne doit pas contenir de doublons.");
        }
        if (!idsSejour.equals(new HashSet<>(demandes))) {
            throw new IllegalArgumentException(
                    "La liste des moments ne correspond pas à ceux du séjour (identifiants invalides ou manquants).");
        }
        var parId = existants.stream().collect(Collectors.toMap(Moment::getId, m -> m));
        for (int i = 0; i < demandes.size(); i++) {
            parId.get(demandes.get(i)).setOrdre(i);
        }
        momentRepository.saveAll(existants);
        return listerMomentsDuSejour(sejourId);
    }

    @Override
    @Transactional
    public void supprimerMoment(int sejourId, int momentId) {
        Moment moment = getMomentEtVerifierSejour(sejourId, momentId);
        if (activiteRepository.existsByMomentId(momentId)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce moment : des activités y sont encore rattachées.");
        }
        momentRepository.delete(moment);
    }

    private Sejour verifierSejourExiste(int sejourId) {
        return sejourRepository.findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
    }

    private Moment getMomentEtVerifierSejour(int sejourId, int momentId) {
        Moment moment = momentRepository.findById(momentId)
                .orElseThrow(() -> new ResourceNotFoundException("Moment non trouvé avec l'ID: " + momentId));
        if (moment.getSejour().getId() != sejourId) {
            throw new ResourceNotFoundException("Le moment n'appartient pas à ce séjour");
        }
        return moment;
    }

    private static String normaliserNom(String nom) {
        return nom == null ? "" : nom.trim();
    }

    private void verifierNomMomentUniquePourSejour(int sejourId, String nom, Integer excludeMomentId) {
        boolean doublon = excludeMomentId == null
                ? momentRepository.existsBySejourIdAndNomIgnoreCase(sejourId, nom)
                : momentRepository.existsBySejourIdAndNomIgnoreCaseAndIdNot(sejourId, nom, excludeMomentId);
        if (doublon) {
            throw new ResourceAlreadyExistsException("Un moment avec ce nom existe déjà pour ce séjour");
        }
    }

    private MomentDto mapToDto(Moment moment) {
        int ordreAffiche =
                moment.getOrdre() != null ? moment.getOrdre() : moment.getId();
        return new MomentDto(moment.getId(), moment.getNom(), moment.getSejour().getId(), ordreAffiche);
    }

    private int prochainOrdrePourSejour(int sejourId) {
        return momentRepository.findBySejourIdOrderChronologique(sejourId).stream()
                        .mapToInt(m -> m.getOrdre() != null ? m.getOrdre() : m.getId())
                        .max()
                        .orElse(-1)
                + 1;
    }
}
