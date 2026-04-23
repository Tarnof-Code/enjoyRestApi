package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotNull;

public record SavePlanningLigneRequest(
        @NotNull(message = "L'ordre d'affichage est obligatoire") Integer ordre,
        String libelleSaisieLibre,
        String libelleRegroupement,
        Integer libelleMomentId,
        Integer libelleHoraireId,
        Integer libelleGroupeId,
        Integer libelleLieuId,
        String libelleUtilisateurTokenId) {}
