package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AffecterOccupantEnfantItemRequest(
        @NotNull(message = "L'identifiant de l'enfant est obligatoire") Integer enfantId,
        @Positive(message = "Le numéro de lit doit être strictement positif") Integer numeroLit
) {}
