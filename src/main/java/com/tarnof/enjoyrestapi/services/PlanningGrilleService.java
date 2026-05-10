package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.*;
import com.tarnof.enjoyrestapi.payload.response.PlanningCelluleDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleDetailDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleSummaryDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningLigneDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PlanningGrilleService {

    List<PlanningGrilleSummaryDto> listerGrilles(int sejourId, String utilisateurTokenId);

    PlanningGrilleDetailDto getGrille(int sejourId, int grilleId, String utilisateurTokenId);

    PlanningGrilleDetailDto creerGrille(int sejourId, SavePlanningGrilleRequest request);

    PlanningGrilleDetailDto modifierGrille(int sejourId, int grilleId, UpdatePlanningGrilleRequest request);

    void supprimerGrille(int sejourId, int grilleId);

    PlanningLigneDto creerLigne(int sejourId, int grilleId, SavePlanningLigneRequest request);

    PlanningLigneDto modifierLigne(int sejourId, int grilleId, int ligneId, UpdatePlanningLigneRequest request);

    void supprimerLigne(int sejourId, int grilleId, int ligneId);

    List<PlanningCelluleDto> remplacerCellules(
            int sejourId,
            int grilleId,
            int ligneId,
            UpsertPlanningCellulesRequest request,
            String modificateurTokenId);

    /**
     * Ajoute ou retire l'utilisateur désigné par {@code utilisateurTokenId} sur une cellule
     * « membre d'équipe » pour un jour donné ; uniquement ce compte peut être ajouté ou retiré.
     *
     * @return une cellule persistante le cas échéant ; vide si aucune cellule après l'opération (ex. dernier animateur parti)
     */
    Optional<PlanningCelluleDto> modifierMaPresenceSurCelluleMembreEquipe(
            int sejourId,
            int grilleId,
            int ligneId,
            LocalDate jour,
            boolean present,
            String utilisateurTokenId);
}
