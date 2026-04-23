package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PlanningCellulePayload(
        @NotNull(message = "La date de la cellule est obligatoire") LocalDate jour,
        /** {@link com.tarnof.enjoyrestapi.entities.Utilisateur#getTokenId() tokenId} des membres (comme pour l’équipe du séjour côté API). */
        List<String> membreTokenIds,
        Integer horaireId,
        String texteLibre,
        Integer momentId,
        Integer groupeId,
        Integer lieuId) {}
