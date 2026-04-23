package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.PlanningLigneLibelleSource;

import java.time.Instant;
import java.util.List;

public record PlanningGrilleDetailDto(
        int id,
        int sejourId,
        String titre,
        String consigneGlobale,
        PlanningLigneLibelleSource sourceLibelleLignes,
        PlanningLigneLibelleSource sourceContenuCellules,
        Instant miseAJour,
        List<PlanningLigneDto> lignes) {}
