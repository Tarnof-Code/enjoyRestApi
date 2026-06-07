package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Moment;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.ReorderMomentsRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveMomentRequest;
import com.tarnof.enjoyrestapi.payload.response.MomentDto;
import com.tarnof.enjoyrestapi.repositories.ActivitePrestataireRepository;
import com.tarnof.enjoyrestapi.repositories.ActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.MomentRepository;
import com.tarnof.enjoyrestapi.repositories.PlanningCelluleRepository;
import com.tarnof.enjoyrestapi.repositories.PlanningLigneRepository;
import com.tarnof.enjoyrestapi.services.MomentService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
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
    private final SejourVerificationService sejourVerificationService;
    private final ActiviteRepository activiteRepository;
    private final ActivitePrestataireRepository activitePrestataireRepository;
    private final PlanningLigneRepository planningLigneRepository;
    private final PlanningCelluleRepository planningCelluleRepository;

    public MomentServiceImpl(
            MomentRepository momentRepository,
            SejourVerificationService sejourVerificationService,
            ActiviteRepository activiteRepository,
            ActivitePrestataireRepository activitePrestataireRepository,
            PlanningLigneRepository planningLigneRepository,
            PlanningCelluleRepository planningCelluleRepository) {
        this.momentRepository = momentRepository;
        this.sejourVerificationService = sejourVerificationService;
        this.activiteRepository = activiteRepository;
        this.activitePrestataireRepository = activitePrestataireRepository;
        this.planningLigneRepository = planningLigneRepository;
        this.planningCelluleRepository = planningCelluleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MomentDto> listerMomentsDuSejour(int sejourId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        return momentRepository.findBySejourIdOrderChronologique(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MomentDto getMoment(int sejourId, int momentId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        return mapToDto(getMomentEtVerifierSejour(sejourId, momentId));
    }

    @Override
    @Transactional
    public MomentDto creerMoment(int sejourId, SaveMomentRequest request) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        String nom = normaliserNom(request.nom());
        verifierNomMomentUniquePourSejour(sejourId, nom, null);
        Moment parent = resoudreParent(sejourId, request.parentId(), null);
        Moment moment = new Moment();
        moment.setNom(nom);
        moment.setSejour(sejour);
        moment.setParent(parent);
        moment.setOrdre(prochainOrdrePourSejour(sejourId));
        return mapToDto(momentRepository.save(moment));
    }

    @Override
    @Transactional
    public MomentDto modifierMoment(int sejourId, int momentId, SaveMomentRequest request) {
        Moment moment = getMomentEtVerifierSejour(sejourId, momentId);
        String nom = normaliserNom(request.nom());
        verifierNomMomentUniquePourSejour(sejourId, nom, momentId);
        Moment parent = resoudreParent(sejourId, request.parentId(), momentId);
        moment.setNom(nom);
        moment.setParent(parent);
        return mapToDto(momentRepository.save(moment));
    }

    @Override
    @Transactional
    public List<MomentDto> reorderMoments(int sejourId, ReorderMomentsRequest request) {
        sejourVerificationService.verifierSejourExiste(sejourId);
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
        return momentRepository.findBySejourIdOrderChronologique(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void supprimerMoment(int sejourId, int momentId) {
        Moment moment = getMomentEtVerifierSejour(sejourId, momentId);
        if (momentRepository.existsByParentId(momentId)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce moment : des sous-moments y sont encore rattachés.");
        }
        if (activiteRepository.existsByMomentId(momentId)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce moment : des activités y sont encore rattachées.");
        }
        if (activitePrestataireRepository.existsByMoments_Id(momentId)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce moment : des activités prestataires y sont encore rattachées.");
        }
        if (planningLigneRepository.existsByLibelleMoment_Id(momentId)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce moment : il est encore utilisé comme libellé de ligne dans une grille de planning.");
        }
        if (planningCelluleRepository.existsByMoments_Id(momentId)) {
            throw new IllegalArgumentException(
                    "Impossible de supprimer ce moment : il est encore utilisé dans des cellules de planning.");
        }
        momentRepository.delete(moment);
    }

    /**
     * Résout et valide le parent demandé. Profondeur de hiérarchie libre : le parent doit
     * appartenir au même séjour et ne pas créer de cycle (un moment ne peut pas être rattaché à
     * lui-même ni à l'un de ses propres descendants).
     */
    private Moment resoudreParent(int sejourId, Integer parentId, Integer momentIdEnCours) {
        if (parentId == null) {
            return null;
        }
        Moment parent = momentRepository.findByIdAndSejourId(parentId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Moment parent non trouvé avec l'ID: " + parentId + " pour ce séjour"));
        if (momentIdEnCours != null) {
            verifierAbsenceDeCycle(parent, momentIdEnCours);
        }
        return parent;
    }

    /**
     * Vérifie qu'aucun cycle n'est introduit en remontant la chaîne des parents : si le moment en
     * cours figure parmi le parent visé ou ses ancêtres, le rattachement est refusé.
     */
    private void verifierAbsenceDeCycle(Moment parent, int momentIdEnCours) {
        Moment courant = parent;
        Set<Integer> visites = new HashSet<>();
        while (courant != null) {
            if (courant.getId().equals(momentIdEnCours)) {
                throw new IllegalArgumentException(
                        "Rattachement impossible : un moment ne peut pas être son propre parent "
                                + "ni être rattaché à l'un de ses sous-moments.");
            }
            if (!visites.add(courant.getId())) {
                break;
            }
            courant = courant.getParent();
        }
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
        Integer parentId = moment.getParent() != null ? moment.getParent().getId() : null;
        return new MomentDto(moment.getId(), moment.getNom(), moment.getSejour().getId(), ordreAffiche, parentId);
    }

    private int prochainOrdrePourSejour(int sejourId) {
        return momentRepository.findBySejourIdOrderChronologique(sejourId).stream()
                        .mapToInt(m -> m.getOrdre() != null ? m.getOrdre() : m.getId())
                        .max()
                        .orElse(-1)
                + 1;
    }
}
