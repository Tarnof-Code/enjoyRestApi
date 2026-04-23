package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.PlanningLigneLibelleSource;
import jakarta.validation.constraints.NotBlank;

public record SavePlanningGrilleRequest(
        @NotBlank(message = "Le titre du planning est obligatoire") String titre,
        String consigneGlobale,
        PlanningLigneLibelleSource sourceLibelleLignes,
        PlanningLigneLibelleSource sourceContenuCellules) {}
