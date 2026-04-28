package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record PlanningCellulePayload(
        @NotNull(message = "La date de la cellule est obligatoire") LocalDate jour,
        /** {@link com.tarnof.enjoyrestapi.entities.Utilisateur#getTokenId() tokenId} des membres (comme pour l’équipe du séjour côté API). */
        List<String> membreTokenIds,
        List<Integer> horaireIds,
        String texteLibre,
        List<Integer> momentIds,
        List<Integer> groupeIds,
        List<Integer> lieuIds) {}
