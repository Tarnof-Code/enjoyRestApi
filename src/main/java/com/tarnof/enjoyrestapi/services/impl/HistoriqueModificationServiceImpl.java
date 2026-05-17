package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.HistoriqueModification;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationActivite;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationCahierInfirmerie;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationPlanningCellule;
import com.tarnof.enjoyrestapi.entities.PlanningLigne;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationAction;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationType;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationActiviteDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationBaseDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationCahierInfirmerieDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationPlanningCelluleDto;
import com.tarnof.enjoyrestapi.repositories.ActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.CahierInfirmerieEntreeRepository;
import com.tarnof.enjoyrestapi.repositories.HistoriqueModificationRepository;
import com.tarnof.enjoyrestapi.repositories.PlanningLigneRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.HistoriqueModificationService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
public class HistoriqueModificationServiceImpl implements HistoriqueModificationService {

    private final HistoriqueModificationRepository historiqueModificationRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PlanningLigneRepository planningLigneRepository;
    private final ActiviteRepository activiteRepository;
    private final CahierInfirmerieEntreeRepository cahierInfirmerieEntreeRepository;
    private final SejourVerificationService sejourVerificationService;

    public HistoriqueModificationServiceImpl(
            HistoriqueModificationRepository historiqueModificationRepository,
            UtilisateurRepository utilisateurRepository,
            PlanningLigneRepository planningLigneRepository,
            ActiviteRepository activiteRepository,
            CahierInfirmerieEntreeRepository cahierInfirmerieEntreeRepository,
            SejourVerificationService sejourVerificationService) {
        this.historiqueModificationRepository = historiqueModificationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.planningLigneRepository = planningLigneRepository;
        this.activiteRepository = activiteRepository;
        this.cahierInfirmerieEntreeRepository = cahierInfirmerieEntreeRepository;
        this.sejourVerificationService = sejourVerificationService;
    }

    @Override
    @Transactional
    public void enregistrerPlanningCellule(
            String modificateurTokenId,
            HistoriqueModificationAction action,
            int planningLigneId,
            LocalDate jour,
            int planningCelluleId,
            String ancienneValeur,
            String nouvelleValeur) {
        Utilisateur modificateur = resoudreModificateur(modificateurTokenId);
        HistoriqueModificationPlanningCellule entree = new HistoriqueModificationPlanningCellule();
        entree.setAction(action);
        entree.setDateModification(Instant.now());
        entree.setModificateur(modificateur);
        entree.setPlanningLigneId(planningLigneId);
        entree.setPlanningJour(jour);
        entree.setPlanningCelluleId(planningCelluleId);
        entree.setAncienneValeur(ancienneValeur);
        entree.setNouvelleValeur(nouvelleValeur);
        historiqueModificationRepository.save(entree);
    }

    @Override
    @Transactional
    public void enregistrerActivite(
            String modificateurTokenId,
            HistoriqueModificationAction action,
            int activiteId,
            String ancienneValeur,
            String nouvelleValeur) {
        Utilisateur modificateur = resoudreModificateur(modificateurTokenId);
        HistoriqueModificationActivite entree = new HistoriqueModificationActivite();
        entree.setAction(action);
        entree.setDateModification(Instant.now());
        entree.setModificateur(modificateur);
        entree.setActiviteId(activiteId);
        entree.setAncienneValeur(ancienneValeur);
        entree.setNouvelleValeur(nouvelleValeur);
        historiqueModificationRepository.save(entree);
    }

    @Override
    @Transactional
    public void enregistrerCahierInfirmerie(
            String modificateurTokenId,
            HistoriqueModificationAction action,
            int entreeId,
            String ancienneValeur,
            String nouvelleValeur) {
        Utilisateur modificateur = resoudreModificateur(modificateurTokenId);
        HistoriqueModificationCahierInfirmerie entree = new HistoriqueModificationCahierInfirmerie();
        entree.setAction(action);
        entree.setDateModification(Instant.now());
        entree.setModificateur(modificateur);
        entree.setCahierInfirmerieEntreeId(entreeId);
        entree.setAncienneValeur(ancienneValeur);
        entree.setNouvelleValeur(nouvelleValeur);
        historiqueModificationRepository.save(entree);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueModificationPlanningCelluleDto> listerHistoriquePlanningCellules(
            int sejourId, int grilleId, int ligneId, LocalDate jour, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        PlanningLigne planningLigne =
                planningLigneRepository
                        .findByIdAndGrille_Id(ligneId, grilleId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Ligne de planning non trouvée avec l'ID: " + ligneId));
        if (planningLigne.getGrille().getSejour().getId() != sejourId) {
            throw new ResourceNotFoundException("Ligne de planning non trouvée avec l'ID: " + ligneId);
        }
        List<? extends HistoriqueModification> entrees =
                jour != null
                        ? historiqueModificationRepository.findPlanningByLigneIdAndJour(ligneId, jour)
                        : historiqueModificationRepository.findPlanningByLigneId(ligneId);
        return entrees.stream().map(this::toDtoPlanningCellule).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueModificationActiviteDto> listerHistoriqueActivite(
            int sejourId, int activiteId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        if (activiteRepository.findByIdAndSejourId(activiteId, sejourId).isEmpty()) {
            throw new ResourceNotFoundException(
                    "Activité non trouvée pour ce séjour (id: " + activiteId + ")");
        }
        return historiqueModificationRepository.findActiviteByActiviteId(activiteId).stream()
                .map(this::toDtoActivite)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueModificationCahierInfirmerieDto> listerHistoriqueCahierInfirmerie(
            int sejourId, int entreeId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        if (cahierInfirmerieEntreeRepository.findByIdAndSejourIdWithEnfantAndCreateur(entreeId, sejourId).isEmpty()) {
            throw new ResourceNotFoundException(
                    "Entrée de cahier d'infirmerie non trouvée pour ce séjour (id: " + entreeId + ")");
        }
        return historiqueModificationRepository.findCahierInfirmerieByEntreeId(entreeId).stream()
                .map(this::toDtoCahierInfirmerie)
                .toList();
    }

    private Utilisateur resoudreModificateur(String modificateurTokenId) {
        return utilisateurRepository
                .findByTokenId(modificateurTokenId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "Utilisateur non trouvé avec le tokenId: " + modificateurTokenId));
    }

    private HistoriqueModificationBaseDto toBaseDto(HistoriqueModification h, HistoriqueModificationType type) {
        Utilisateur u = h.getModificateur();
        return new HistoriqueModificationBaseDto(
                h.getId(),
                type,
                h.getDateModification(),
                u.getTokenId(),
                u.getNom(),
                u.getPrenom(),
                h.getAction(),
                h.getAncienneValeur(),
                h.getNouvelleValeur());
    }

    private HistoriqueModificationPlanningCelluleDto toDtoPlanningCellule(HistoriqueModification h) {
        if (!(h instanceof HistoriqueModificationPlanningCellule p)) {
            throw new IllegalStateException(
                    "Type d'historique inattendu pour planning cellule: " + h.getClass().getName());
        }
        HistoriqueModificationBaseDto base = toBaseDto(h, HistoriqueModificationType.PLANNING_CELLULE);
        return new HistoriqueModificationPlanningCelluleDto(
                base,
                p.getPlanningLigneId(),
                p.getPlanningJour(),
                p.getPlanningCelluleId());
    }

    private HistoriqueModificationActiviteDto toDtoActivite(HistoriqueModification h) {
        if (!(h instanceof HistoriqueModificationActivite a)) {
            throw new IllegalStateException(
                    "Type d'historique inattendu pour activité: " + h.getClass().getName());
        }
        HistoriqueModificationBaseDto base = toBaseDto(h, HistoriqueModificationType.ACTIVITE);
        return new HistoriqueModificationActiviteDto(base, a.getActiviteId());
    }

    private HistoriqueModificationCahierInfirmerieDto toDtoCahierInfirmerie(HistoriqueModification h) {
        if (!(h instanceof HistoriqueModificationCahierInfirmerie c)) {
            throw new IllegalStateException(
                    "Type d'historique inattendu pour cahier d'infirmerie: " + h.getClass().getName());
        }
        HistoriqueModificationBaseDto base = toBaseDto(h, HistoriqueModificationType.CAHIER_INFIRMERIE);
        return new HistoriqueModificationCahierInfirmerieDto(base, c.getCahierInfirmerieEntreeId());
    }
}
