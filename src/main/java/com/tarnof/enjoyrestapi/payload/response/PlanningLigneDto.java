package com.tarnof.enjoyrestapi.payload.response;

import java.util.List;

public record PlanningLigneDto(
        int id,
        int ordre,
        String libelleSaisieLibre,
        String libelleRegroupement,
        Integer libelleMomentId,
        Integer libelleHoraireId,
        Integer libelleGroupeId,
        Integer libelleLieuId,
        String libelleUtilisateurTokenId,
        List<PlanningCelluleDto> cellules) {}
