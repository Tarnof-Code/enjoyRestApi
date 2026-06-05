package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.HistoriqueModification;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationActivite;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationCahierInfirmerie;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationActivitePrestataire;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationChambre;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationPlanningCellule;
import com.tarnof.enjoyrestapi.entities.PlanningLigne;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationAction;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationType;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationActiviteDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationActivitePrestataireDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationBaseDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationCahierInfirmerieDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationChambreDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationPlanningCelluleDto;
import com.tarnof.enjoyrestapi.repositories.ActivitePrestataireRepository;
import com.tarnof.enjoyrestapi.repositories.ActiviteRepository;
import com.tarnof.enjoyrestapi.repositories.CahierInfirmerieEntreeRepository;
import com.tarnof.enjoyrestapi.repositories.ChambreRepository;
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
    private final ChambreRepository chambreRepository;
    private final ActivitePrestataireRepository activitePrestataireRepository;
    private final SejourVerificationService sejourVerificationService;

    public HistoriqueModificationServiceImpl(
            HistoriqueModificationRepository historiqueModificationRepository,
            UtilisateurRepository utilisateurRepository,
            PlanningLigneRepository planningLigneRepository,
            ActiviteRepository activiteRepository,
            CahierInfirmerieEntreeRepository cahierInfirmerieEntreeRepository,
            ChambreRepository chambreRepository,
            ActivitePrestataireRepository activitePrestataireRepository,
            SejourVerificationService sejourVerificationService) {
        this.historiqueModificationRepository = historiqueModificationRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.planningLigneRepository = planningLigneRepository;
        this.activiteRepository = activiteRepository;
        this.cahierInfirmerieEntreeRepository = cahierInfirmerieEntreeRepository;
        this.chambreRepository = chambreRepository;
        this.activitePrestataireRepository = activitePrestataireRepository;
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
    @Transactional
    public void enregistrerChambre(
            String modificateurTokenId,
            HistoriqueModificationAction action,
            int chambreId,
            String ancienneValeur,
            String nouvelleValeur) {
        Utilisateur modificateur = resoudreModificateur(modificateurTokenId);
        HistoriqueModificationChambre entree = new HistoriqueModificationChambre();
        entree.setAction(action);
        entree.setDateModification(Instant.now());
        entree.setModificateur(modificateur);
        entree.setChambreId(chambreId);
        entree.setAncienneValeur(ancienneValeur);
        entree.setNouvelleValeur(nouvelleValeur);
        historiqueModificationRepository.save(entree);
    }

    @Override
    @Transactional
    public void enregistrerActivitePrestataire(
            String modificateurTokenId,
            HistoriqueModificationAction action,
            int activitePrestataireId,
            String ancienneValeur,
            String nouvelleValeur) {
        Utilisateur modificateur = resoudreModificateur(modificateurTokenId);
        HistoriqueModificationActivitePrestataire entree = new HistoriqueModificationActivitePrestataire();
        entree.setAction(action);
        entree.setDateModification(Instant.now());
        entree.setModificateur(modificateur);
        entree.setActivitePrestataireId(activitePrestataireId);
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

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueModificationChambreDto> listerHistoriqueChambre(
            int sejourId, int chambreId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        if (chambreRepository.findByIdAndSejourId(chambreId, sejourId).isEmpty()) {
            throw new ResourceNotFoundException("Chambre non trouvée avec l'ID: " + chambreId);
        }
        return historiqueModificationRepository.findChambreByChambreId(chambreId).stream()
                .map(this::toDtoChambre)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistoriqueModificationActivitePrestataireDto> listerHistoriqueActivitePrestataire(
            int sejourId, int activitePrestataireId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        if (activitePrestataireRepository.findByIdAndSejour_Id(activitePrestataireId, sejourId).isEmpty()) {
            throw new ResourceNotFoundException(
                    "Activité prestataire non trouvée pour ce séjour (id: " + activitePrestataireId + ")");
        }
        return historiqueModificationRepository
                .findActivitePrestataireByActivitePrestataireId(activitePrestataireId)
                .stream()
                .map(this::toDtoActivitePrestataire)
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

    private HistoriqueModificationChambreDto toDtoChambre(HistoriqueModification h) {
        if (!(h instanceof HistoriqueModificationChambre c)) {
            throw new IllegalStateException(
                    "Type d'historique inattendu pour chambre: " + h.getClass().getName());
        }
        HistoriqueModificationBaseDto base = toBaseDto(h, HistoriqueModificationType.CHAMBRE);
        return new HistoriqueModificationChambreDto(base, c.getChambreId());
    }

    private HistoriqueModificationActivitePrestataireDto toDtoActivitePrestataire(HistoriqueModification h) {
        if (!(h instanceof HistoriqueModificationActivitePrestataire a)) {
            throw new IllegalStateException(
                    "Type d'historique inattendu pour activité prestataire: " + h.getClass().getName());
        }
        HistoriqueModificationBaseDto base = toBaseDto(h, HistoriqueModificationType.ACTIVITE_PRESTATAIRE);
        return new HistoriqueModificationActivitePrestataireDto(base, a.getActivitePrestataireId());
    }
}
