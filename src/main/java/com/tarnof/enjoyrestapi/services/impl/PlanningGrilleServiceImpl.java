package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.*;
import com.tarnof.enjoyrestapi.enums.PlanningLigneLibelleSource;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.*;
import com.tarnof.enjoyrestapi.payload.response.PlanningCelluleDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleDetailDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleSummaryDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningLigneDto;
import com.tarnof.enjoyrestapi.repositories.*;
import com.tarnof.enjoyrestapi.services.PlanningGrilleService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class PlanningGrilleServiceImpl implements PlanningGrilleService {

    /** Libellés d'erreur : {@code libelleHoraireId} (lignes) vs {@code horaireId} (cellules). */
    private enum ReferencesMetierContext {
        LIGNE,
        CELLULE;

        String nomChampHoraire() {
            return this == CELLULE ? "horaireId" : "libelleHoraireId";
        }
    }

    private final PlanningGrilleRepository planningGrilleRepository;
    private final PlanningLigneRepository planningLigneRepository;
    private final PlanningCelluleRepository planningCelluleRepository;
    private final SejourVerificationService sejourVerificationService;
    private final MomentRepository momentRepository;
    private final HoraireRepository horaireRepository;
    private final GroupeRepository groupeRepository;
    private final LieuRepository lieuRepository;
    private final SejourEquipeRepository sejourEquipeRepository;
    private final UtilisateurRepository utilisateurRepository;

    public PlanningGrilleServiceImpl(
            PlanningGrilleRepository planningGrilleRepository,
            PlanningLigneRepository planningLigneRepository,
            PlanningCelluleRepository planningCelluleRepository,
            SejourVerificationService sejourVerificationService,
            MomentRepository momentRepository,
            HoraireRepository horaireRepository,
            GroupeRepository groupeRepository,
            LieuRepository lieuRepository,
            SejourEquipeRepository sejourEquipeRepository,
            UtilisateurRepository utilisateurRepository) {
        this.planningGrilleRepository = planningGrilleRepository;
        this.planningLigneRepository = planningLigneRepository;
        this.planningCelluleRepository = planningCelluleRepository;
        this.sejourVerificationService = sejourVerificationService;
        this.momentRepository = momentRepository;
        this.horaireRepository = horaireRepository;
        this.groupeRepository = groupeRepository;
        this.lieuRepository = lieuRepository;
        this.sejourEquipeRepository = sejourEquipeRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanningGrilleSummaryDto> listerGrilles(int sejourId) {
        sejourVerificationService.verifierSejourExiste(sejourId);
        return planningGrilleRepository.findBySejour_IdOrderByMiseAJourDesc(sejourId).stream()
                .map(this::toSummaryDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PlanningGrilleDetailDto getGrille(int sejourId, int grilleId) {
        PlanningGrille grille = getGrilleEtVerifierSejour(sejourId, grilleId);
        return construireDetail(sejourId, grille);
    }

    @Override
    @Transactional
    public PlanningGrilleDetailDto creerGrille(int sejourId, SavePlanningGrilleRequest request) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        PlanningGrille grille = new PlanningGrille();
        grille.setSejour(sejour);
        grille.setTitre(request.titre().trim());
        grille.setConsigneGlobale(trimToNull(request.consigneGlobale()));
        PlanningLigneLibelleSource libelles = request.sourceLibelleLignes();
        grille.setSourceLibelleLignes(libelles);
        grille.setSourceContenuCellules(
                request.sourceContenuCellules() != null
                        ? request.sourceContenuCellules()
                        : PlanningLigneLibelleSource.SAISIE_LIBRE);
        touch(grille);
        PlanningGrille sauve = planningGrilleRepository.save(grille);
        return construireDetail(sejourId, sauve);
    }

    @Override
    @Transactional
    public PlanningGrilleDetailDto modifierGrille(int sejourId, int grilleId, UpdatePlanningGrilleRequest request) {
        PlanningGrille grille = getGrilleEtVerifierSejour(sejourId, grilleId);
        grille.setTitre(request.titre().trim());
        grille.setConsigneGlobale(trimToNull(request.consigneGlobale()));
        PlanningLigneLibelleSource libelles = request.sourceLibelleLignes();
        grille.setSourceLibelleLignes(libelles);
        if (request.sourceContenuCellules() != null) {
            grille.setSourceContenuCellules(request.sourceContenuCellules());
        }
        touch(grille);
        planningGrilleRepository.save(grille);
        return construireDetail(sejourId, grille);
    }

    @Override
    @Transactional
    public void supprimerGrille(int sejourId, int grilleId) {
        PlanningGrille grille = getGrilleEtVerifierSejour(sejourId, grilleId);
        planningGrilleRepository.delete(grille);
    }

    @Override
    @Transactional
    public PlanningLigneDto creerLigne(int sejourId, int grilleId, SavePlanningLigneRequest request) {
        PlanningGrille grille = getGrilleEtVerifierSejour(sejourId, grilleId);
        PlanningLigne ligne = new PlanningLigne();
        ligne.setGrille(grille);
        ligne.setOrdre(request.ordre());
        appliquerContenuLigneSelonGrille(
                sejourId,
                grille,
                ligne,
                request.libelleSaisieLibre(),
                request.libelleRegroupement(),
                request.libelleMomentId(),
                request.libelleHoraireId(),
                request.libelleGroupeId(),
                request.libelleLieuId(),
                request.libelleUtilisateurTokenId());
        touch(grille);
        PlanningLigne sauve = planningLigneRepository.save(ligne);
        return toLigneDto(sauve, Map.of());
    }

    @Override
    @Transactional
    public PlanningLigneDto modifierLigne(
            int sejourId, int grilleId, int ligneId, UpdatePlanningLigneRequest request) {
        PlanningGrille grille = getGrilleEtVerifierSejour(sejourId, grilleId);
        PlanningLigne ligne = getLigneEtVerifierGrille(grilleId, ligneId);
        ligne.setOrdre(request.ordre());
        appliquerContenuLigneSelonGrille(
                sejourId,
                grille,
                ligne,
                request.libelleSaisieLibre(),
                request.libelleRegroupement(),
                request.libelleMomentId(),
                request.libelleHoraireId(),
                request.libelleGroupeId(),
                request.libelleLieuId(),
                request.libelleUtilisateurTokenId());
        touch(grille);
        planningLigneRepository.save(ligne);
        Map<Integer, List<PlanningCellule>> cellulesParLigne = chargerCellulesPourLignes(List.of(ligneId));
        return toLigneDto(ligne, cellulesParLigne);
    }

    @Override
    @Transactional
    public void supprimerLigne(int sejourId, int grilleId, int ligneId) {
        PlanningGrille grille = getGrilleEtVerifierSejour(sejourId, grilleId);
        PlanningLigne ligne = getLigneEtVerifierGrille(grilleId, ligneId);
        touch(grille);
        planningLigneRepository.delete(ligne);
    }

    @Override
    @Transactional
    public List<PlanningCelluleDto> remplacerCellules(
            int sejourId, int grilleId, int ligneId, UpsertPlanningCellulesRequest request) {
        PlanningGrille grille = getGrilleEtVerifierSejour(sejourId, grilleId);
        Sejour sejour = grille.getSejour();
        PlanningLigne ligne = getLigneEtVerifierGrille(grilleId, ligneId);
        verifierPasDeJourDuplique(request);
        for (PlanningCellulePayload payload : request.cellules()) {
            if (cellulePayloadVide(payload)) {
                planningCelluleRepository
                        .findByLigne_IdAndJour(ligneId, payload.jour())
                        .ifPresent(planningCelluleRepository::delete);
            } else {
                PlanningCellule cellule = planningCelluleRepository
                        .findByLigne_IdAndJour(ligneId, payload.jour())
                        .orElseGet(() -> nouvelleCellule(ligne, payload.jour()));
                cellule.setLigne(ligne);
                cellule.setJour(payload.jour());
                appliquerReferencesMetierCellule(sejourId, cellule, sourceContenuCellulesEffectif(grille), payload);
                cellule.setTexteLibre(trimToNull(payload.texteLibre()));
                cellule.getAnimateursAssignes().clear();
                cellule.getAnimateursAssignes().addAll(chargerMembresCelluleValides(sejour, payload.membreTokenIds()));
                planningCelluleRepository.save(cellule);
            }
        }
        touch(grille);
        List<PlanningCellule> cellules =
                planningCelluleRepository.findByLigne_IdIn(List.of(ligneId));
        return cellules.stream()
                .sorted(Comparator.comparing(PlanningCellule::getJour))
                .map(this::toCelluleDto)
                .toList();
    }

    private void verifierPasDeJourDuplique(UpsertPlanningCellulesRequest request) {
        Set<LocalDate> vus = new HashSet<>();
        for (PlanningCellulePayload p : request.cellules()) {
            if (!vus.add(p.jour())) {
                throw new IllegalArgumentException("La même date ne peut apparaître qu'une fois dans la requête");
            }
        }
    }

    private static PlanningCellule nouvelleCellule(PlanningLigne ligne, LocalDate jour) {
        PlanningCellule c = new PlanningCellule();
        c.setLigne(ligne);
        c.setJour(jour);
        return c;
    }

    private Map<Integer, List<PlanningCellule>> chargerCellulesPourLignes(List<Integer> ligneIds) {
        if (ligneIds.isEmpty()) {
            return Map.of();
        }
        return planningCelluleRepository.findByLigne_IdIn(ligneIds).stream()
                .collect(Collectors.groupingBy(c -> c.getLigne().getId()));
    }

    private PlanningGrilleDetailDto construireDetail(int sejourId, PlanningGrille grille) {
        List<PlanningLigne> lignesBrutes = planningLigneRepository.findByGrille_Id(grille.getId());
        List<PlanningLigne> lignes = trierLignesPourAffichage(lignesBrutes);
        List<Integer> ids = lignes.stream().map(PlanningLigne::getId).toList();
        Map<Integer, List<PlanningCellule>> parLigne = chargerCellulesPourLignes(ids);
        List<PlanningLigneDto> ligneDtos = lignes.stream().map(l -> toLigneDto(l, parLigne)).toList();
        PlanningLigneLibelleSource srcCellules = sourceContenuCellulesEffectif(grille);
        return new PlanningGrilleDetailDto(
                grille.getId(),
                sejourId,
                grille.getTitre(),
                grille.getConsigneGlobale(),
                grille.getSourceLibelleLignes(),
                srcCellules,
                grille.getMiseAJour(),
                ligneDtos);
    }

    private static PlanningLigneLibelleSource sourceContenuCellulesEffectif(PlanningGrille grille) {
        return grille.getSourceContenuCellules() != null
                ? grille.getSourceContenuCellules()
                : PlanningLigneLibelleSource.SAISIE_LIBRE;
    }

    private void appliquerReferencesMetierCellule(
            int sejourId,
            PlanningCellule cellule,
            PlanningLigneLibelleSource type,
            PlanningCellulePayload payload) {
        cellule.setMoment(null);
        cellule.setHoraire(null);
        cellule.setGroupe(null);
        cellule.setLieu(null);
        verifierIdsCoherentsAvecSource(
                type,
                payload.momentId(),
                payload.horaireId(),
                payload.groupeId(),
                payload.lieuId(),
                null,
                ReferencesMetierContext.CELLULE);
        if (type == PlanningLigneLibelleSource.MEMBRE_EQUIPE) {
            if (payload.membreTokenIds() == null
                    || payload.membreTokenIds().isEmpty()
                    || payload.membreTokenIds().stream().allMatch(s -> s == null || s.isBlank())) {
                throw new IllegalArgumentException(
                        "Pour MEMBRE_EQUIPE, renseignez au moins un tokenId dans membreTokenIds (membre du séjour).");
            }
        }
        switch (type) {
            case SAISIE_LIBRE, MEMBRE_EQUIPE -> {
            }
            case GROUPE ->
                    cellule.setGroupe(resoudreGroupe(Objects.requireNonNull(payload.groupeId()), sejourId));
            case LIEU -> cellule.setLieu(resoudreLieu(Objects.requireNonNull(payload.lieuId()), sejourId));
            case HORAIRE ->
                    cellule.setHoraire(
                            resoudreHoraireNullable(Objects.requireNonNull(payload.horaireId()), sejourId));
            case MOMENT ->
                    cellule.setMoment(resoudreMoment(Objects.requireNonNull(payload.momentId()), sejourId));
        }
    }

    private static List<PlanningLigne> trierLignesPourAffichage(List<PlanningLigne> lignes) {
        return lignes.stream().sorted(comparateurLignesMemeNiveau()).toList();
    }

    /**
     * Regroupe les lignes qui partagent {@link PlanningLigne#getLibelleRegroupement()} (non null), puis ordre puis id.
     */
    private static Comparator<PlanningLigne> comparateurLignesMemeNiveau() {
        return Comparator.comparing(PlanningLigne::getLibelleRegroupement, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER))
                .thenComparing(PlanningLigne::getOrdre, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(PlanningLigne::getId);
    }

    private PlanningGrille getGrilleEtVerifierSejour(int sejourId, int grilleId) {
        sejourVerificationService.verifierSejourExiste(sejourId);
        return planningGrilleRepository
                .findByIdAndSejour_Id(grilleId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Planning non trouvé avec l'ID: " + grilleId));
    }

    private PlanningLigne getLigneEtVerifierGrille(int grilleId, int ligneId) {
        return planningLigneRepository
                .findByIdAndGrille_Id(ligneId, grilleId)
                .orElseThrow(() -> new ResourceNotFoundException("Ligne de planning non trouvée avec l'ID: " + ligneId));
    }

    /**
     * Contenu de ligne : si la grille n'a pas de {@code sourceLibelleLignes}, aucun libellé ni référence métier.
     */
    private void appliquerContenuLigneSelonGrille(
            int sejourId,
            PlanningGrille grille,
            PlanningLigne ligne,
            String libelleSaisieLibreBrut,
            String libelleRegroupementBrut,
            Integer libelleMomentId,
            Integer libelleHoraireId,
            Integer libelleGroupeId,
            Integer libelleLieuId,
            String libelleUtilisateurTokenId) {
        if (grille.getSourceLibelleLignes() == null) {
            ligne.setLibelleRegroupement(trimToNull(libelleRegroupementBrut));
            ligne.setLibelleMoment(null);
            ligne.setLibelleHoraire(null);
            ligne.setLibelleGroupe(null);
            ligne.setLibelleLieu(null);
            ligne.setLibelleUtilisateur(null);
            if (libelleMomentId != null
                    || libelleHoraireId != null
                    || libelleGroupeId != null
                    || libelleLieuId != null
                    || trimToNull(libelleUtilisateurTokenId) != null) {
                throw new IllegalArgumentException(
                        "Ce planning n'a pas de type de libellé de ligne : ne renseignez pas libelleMomentId, libelleHoraireId, libelleGroupeId, libelleLieuId ni libelleUtilisateurTokenId.");
            }
            if (trimToNull(libelleSaisieLibreBrut) != null) {
                throw new IllegalArgumentException(
                        "Ce planning n'a pas de colonne libellé de ligne : ne renseignez pas libelleSaisieLibre.");
            }
            ligne.setLibelleSaisieLibre(null);
            return;
        }
        appliquerContenuLigne(
                sejourId,
                ligne,
                grille.getSourceLibelleLignes(),
                libelleSaisieLibreBrut,
                libelleRegroupementBrut,
                libelleMomentId,
                libelleHoraireId,
                libelleGroupeId,
                libelleLieuId,
                libelleUtilisateurTokenId);
    }

    private void appliquerContenuLigne(
            int sejourId,
            PlanningLigne ligne,
            PlanningLigneLibelleSource source,
            String libelleSaisieLibreBrut,
            String libelleRegroupementBrut,
            Integer libelleMomentId,
            Integer libelleHoraireId,
            Integer libelleGroupeId,
            Integer libelleLieuId,
            String libelleUtilisateurTokenId) {
        ligne.setLibelleRegroupement(trimToNull(libelleRegroupementBrut));
        ligne.setLibelleMoment(null);
        ligne.setLibelleHoraire(null);
        ligne.setLibelleGroupe(null);
        ligne.setLibelleLieu(null);
        ligne.setLibelleUtilisateur(null);

        verifierIdsCoherentsAvecSource(
                source,
                libelleMomentId,
                libelleHoraireId,
                libelleGroupeId,
                libelleLieuId,
                libelleUtilisateurTokenId,
                ReferencesMetierContext.LIGNE);

        switch (source) {
            case SAISIE_LIBRE -> ligne.setLibelleSaisieLibre(trimToNull(libelleSaisieLibreBrut));
            case GROUPE -> {
                Groupe g = resoudreGroupe(Objects.requireNonNull(libelleGroupeId), sejourId);
                ligne.setLibelleGroupe(g);
                ligne.setLibelleSaisieLibre(trimToNull(libelleSaisieLibreBrut));
            }
            case LIEU -> {
                Lieu lieu = resoudreLieu(Objects.requireNonNull(libelleLieuId), sejourId);
                ligne.setLibelleLieu(lieu);
                ligne.setLibelleSaisieLibre(trimToNull(libelleSaisieLibreBrut));
            }
            case HORAIRE -> {
                Horaire h = resoudreHoraireNullable(Objects.requireNonNull(libelleHoraireId), sejourId);
                ligne.setLibelleHoraire(h);
                ligne.setLibelleSaisieLibre(trimToNull(libelleSaisieLibreBrut));
            }
            case MOMENT -> {
                Moment m = resoudreMoment(Objects.requireNonNull(libelleMomentId), sejourId);
                ligne.setLibelleMoment(m);
                ligne.setLibelleSaisieLibre(trimToNull(libelleSaisieLibreBrut));
            }
            case MEMBRE_EQUIPE -> {
                Utilisateur u =
                        resoudreUtilisateurMembreLigne(Objects.requireNonNull(trimToNull(libelleUtilisateurTokenId)), sejourId);
                ligne.setLibelleUtilisateur(u);
                ligne.setLibelleSaisieLibre(trimToNull(libelleSaisieLibreBrut));
            }
        }
    }

    private static void verifierIdsCoherentsAvecSource(
            PlanningLigneLibelleSource source,
            Integer libelleMomentId,
            Integer libelleHoraireId,
            Integer libelleGroupeId,
            Integer libelleLieuId,
            String libelleUtilisateurTokenId,
            ReferencesMetierContext ctx) {
        String h = ctx.nomChampHoraire();
        String tokenMembreLigne = trimToNull(libelleUtilisateurTokenId);
        switch (source) {
            case SAISIE_LIBRE -> {
                if (libelleMomentId != null
                        || libelleHoraireId != null
                        || libelleGroupeId != null
                        || libelleLieuId != null
                        || tokenMembreLigne != null) {
                    throw new IllegalArgumentException(
                            "Pour SAISIE_LIBRE, ne renseignez pas libelleMomentId, "
                                    + h
                                    + ", libelleGroupeId, libelleLieuId ni libelleUtilisateurTokenId");
                }
            }
            case GROUPE -> {
                if (libelleGroupeId == null) {
                    throw new IllegalArgumentException("libelleGroupeId est obligatoire lorsque la source est GROUPE");
                }
                if (libelleMomentId != null
                        || libelleHoraireId != null
                        || libelleLieuId != null
                        || tokenMembreLigne != null) {
                    throw new IllegalArgumentException(
                            "Pour GROUPE, seul libelleGroupeId doit être renseigné parmi les références métier");
                }
            }
            case LIEU -> {
                if (libelleLieuId == null) {
                    throw new IllegalArgumentException("libelleLieuId est obligatoire lorsque la source est LIEU");
                }
                if (libelleMomentId != null
                        || libelleHoraireId != null
                        || libelleGroupeId != null
                        || tokenMembreLigne != null) {
                    throw new IllegalArgumentException(
                            "Pour LIEU, seul libelleLieuId doit être renseigné parmi les références métier");
                }
            }
            case HORAIRE -> {
                if (libelleHoraireId == null) {
                    throw new IllegalArgumentException(h + " est obligatoire lorsque la source est HORAIRE");
                }
                if (libelleMomentId != null
                        || libelleGroupeId != null
                        || libelleLieuId != null
                        || tokenMembreLigne != null) {
                    throw new IllegalArgumentException(
                            "Pour HORAIRE, seul " + h + " doit être renseigné parmi les références métier");
                }
            }
            case MOMENT -> {
                if (libelleMomentId == null) {
                    throw new IllegalArgumentException("libelleMomentId est obligatoire lorsque la source est MOMENT");
                }
                if (libelleHoraireId != null
                        || libelleGroupeId != null
                        || libelleLieuId != null
                        || tokenMembreLigne != null) {
                    throw new IllegalArgumentException(
                            "Pour MOMENT, seul libelleMomentId doit être renseigné parmi les références métier");
                }
            }
            case MEMBRE_EQUIPE -> {
                if (ctx == ReferencesMetierContext.CELLULE) {
                    if (tokenMembreLigne != null) {
                        throw new IllegalArgumentException(
                                "Pour MEMBRE_EQUIPE sur les cellules, n'utilisez pas libelleUtilisateurTokenId (utilisez membreTokenIds).");
                    }
                    if (libelleMomentId != null
                            || libelleHoraireId != null
                            || libelleGroupeId != null
                            || libelleLieuId != null) {
                        throw new IllegalArgumentException(
                                "Pour MEMBRE_EQUIPE, ne renseignez pas libelleMomentId, "
                                        + h
                                        + ", libelleGroupeId ni libelleLieuId (utilisez membreTokenIds).");
                    }
                } else {
                    if (tokenMembreLigne == null) {
                        throw new IllegalArgumentException(
                                "libelleUtilisateurTokenId est obligatoire lorsque la source de libellé de ligne est MEMBRE_EQUIPE");
                    }
                    if (libelleMomentId != null
                            || libelleHoraireId != null
                            || libelleGroupeId != null
                            || libelleLieuId != null) {
                        throw new IllegalArgumentException(
                                "Pour MEMBRE_EQUIPE, seul libelleUtilisateurTokenId doit être renseigné parmi les références métier des lignes");
                    }
                }
            }
        }
    }

    private Moment resoudreMoment(Integer momentId, int sejourId) {
        if (momentId == null) {
            return null;
        }
        return momentRepository
                .findByIdAndSejourId(momentId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Moment non trouvé avec l'ID: " + momentId));
    }

    private Horaire resoudreHoraireNullable(Integer horaireId, int sejourId) {
        if (horaireId == null) {
            return null;
        }
        return horaireRepository
                .findByIdAndSejourId(horaireId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Horaire non trouvé avec l'ID: " + horaireId));
    }

    private Groupe resoudreGroupe(Integer groupeId, int sejourId) {
        if (groupeId == null) {
            return null;
        }
        return groupeRepository
                .findByIdAndSejourId(groupeId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Groupe non trouvé avec l'ID: " + groupeId));
    }

    private Lieu resoudreLieu(Integer lieuId, int sejourId) {
        if (lieuId == null) {
            return null;
        }
        return lieuRepository
                .findByIdAndSejourId(lieuId, sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Lieu non trouvé avec l'ID: " + lieuId));
    }

    private Utilisateur resoudreUtilisateurMembreLigne(String tokenId, int sejourId) {
        Utilisateur u =
                utilisateurRepository
                        .findByTokenId(tokenId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Utilisateur non trouvé avec le tokenId: " + tokenId));
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        Utilisateur directeur = sejour.getDirecteur();
        boolean estDirecteurDuSejour = directeur != null && directeur.getId() == u.getId();
        if (!estDirecteurDuSejour
                && !sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(sejourId, u.getId())) {
            throw new IllegalArgumentException(
                    "L'utilisateur choisi (libelleUtilisateurTokenId) ne fait pas partie de l'équipe de ce séjour");
        }
        return u;
    }

    private Set<Utilisateur> chargerMembresCelluleValides(Sejour sejour, List<String> membreTokenIds) {
        if (membreTokenIds == null || membreTokenIds.isEmpty()) {
            return Set.of();
        }
        LinkedHashSet<String> uniques = new LinkedHashSet<>();
        for (String raw : membreTokenIds) {
            if (raw != null) {
                String t = raw.trim();
                if (!t.isEmpty()) {
                    uniques.add(t);
                }
            }
        }
        if (uniques.isEmpty()) {
            return Set.of();
        }
        int sejourId = sejour.getId();
        Utilisateur directeur = sejour.getDirecteur();
        List<Utilisateur> result = new ArrayList<>();
        for (String tokenId : uniques) {
            Utilisateur u =
                    utilisateurRepository
                            .findByTokenId(tokenId)
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Membre non trouvé avec le tokenId: " + tokenId));
            boolean estDirecteurDuSejour = directeur != null && directeur.getId() == u.getId();
            if (!estDirecteurDuSejour) {
                if (!sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(sejourId, u.getId())) {
                    throw new IllegalArgumentException(
                            "L'utilisateur « " + tokenId + " » ne fait pas partie de l'équipe de ce séjour");
                }
            }
            result.add(u);
        }
        return new HashSet<>(result);
    }

    private static boolean cellulePayloadVide(PlanningCellulePayload payload) {
        boolean pasMembres =
                payload.membreTokenIds() == null
                        || payload.membreTokenIds().isEmpty()
                        || payload.membreTokenIds().stream()
                                .allMatch(s -> s == null || s.isBlank());
        boolean pasHoraire = payload.horaireId() == null;
        boolean pasTexte = payload.texteLibre() == null || payload.texteLibre().isBlank();
        boolean pasMoment = payload.momentId() == null;
        boolean pasGroupe = payload.groupeId() == null;
        boolean pasLieu = payload.lieuId() == null;
        return pasMembres && pasHoraire && pasTexte && pasMoment && pasGroupe && pasLieu;
    }

    private void touch(PlanningGrille grille) {
        grille.setMiseAJour(Instant.now());
    }

    private PlanningGrilleSummaryDto toSummaryDto(PlanningGrille grille) {
        return new PlanningGrilleSummaryDto(
                grille.getId(), grille.getSejour().getId(), grille.getTitre(), grille.getMiseAJour());
    }

    private PlanningLigneDto toLigneDto(PlanningLigne ligne, Map<Integer, List<PlanningCellule>> cellulesParLigne) {
        List<PlanningCelluleDto> celluleDtos = cellulesParLigne
                .getOrDefault(ligne.getId(), List.of())
                .stream()
                .sorted(Comparator.comparing(PlanningCellule::getJour))
                .map(this::toCelluleDto)
                .toList();
        return new PlanningLigneDto(
                ligne.getId(),
                ligne.getOrdre(),
                ligne.getLibelleSaisieLibre(),
                ligne.getLibelleRegroupement(),
                ligne.getLibelleMoment() == null ? null : ligne.getLibelleMoment().getId(),
                ligne.getLibelleHoraire() == null ? null : ligne.getLibelleHoraire().getId(),
                ligne.getLibelleGroupe() == null ? null : ligne.getLibelleGroupe().getId(),
                ligne.getLibelleLieu() == null ? null : ligne.getLibelleLieu().getId(),
                ligne.getLibelleUtilisateur() == null ? null : ligne.getLibelleUtilisateur().getTokenId(),
                celluleDtos);
    }

    private PlanningCelluleDto toCelluleDto(PlanningCellule cellule) {
        List<String> membreTokenIds =
                cellule.getAnimateursAssignes().stream()
                        .map(Utilisateur::getTokenId)
                        .filter(Objects::nonNull)
                        .sorted()
                        .toList();
        Horaire h = cellule.getHoraire();
        return new PlanningCelluleDto(
                cellule.getId(),
                cellule.getJour(),
                membreTokenIds,
                h == null ? null : h.getId(),
                h == null ? null : h.getLibelle(),
                cellule.getMoment() == null ? null : cellule.getMoment().getId(),
                cellule.getGroupe() == null ? null : cellule.getGroupe().getId(),
                cellule.getLieu() == null ? null : cellule.getLieu().getId(),
                cellule.getTexteLibre());
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
