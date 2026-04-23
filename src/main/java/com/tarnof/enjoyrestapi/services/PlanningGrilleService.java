package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.*;
import com.tarnof.enjoyrestapi.payload.response.PlanningCelluleDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleDetailDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleSummaryDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningLigneDto;

import java.util.List;

public interface PlanningGrilleService {

    List<PlanningGrilleSummaryDto> listerGrilles(int sejourId);

    PlanningGrilleDetailDto getGrille(int sejourId, int grilleId);

    PlanningGrilleDetailDto creerGrille(int sejourId, SavePlanningGrilleRequest request);

    PlanningGrilleDetailDto modifierGrille(int sejourId, int grilleId, UpdatePlanningGrilleRequest request);

    void supprimerGrille(int sejourId, int grilleId);

    PlanningLigneDto creerLigne(int sejourId, int grilleId, SavePlanningLigneRequest request);

    PlanningLigneDto modifierLigne(int sejourId, int grilleId, int ligneId, UpdatePlanningLigneRequest request);

    void supprimerLigne(int sejourId, int grilleId, int ligneId);

    List<PlanningCelluleDto> remplacerCellules(
            int sejourId, int grilleId, int ligneId, UpsertPlanningCellulesRequest request);
}
