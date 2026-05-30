package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record AffecterOccupantEquipeItemRequest(
        @NotBlank(message = "L'identifiant du membre est obligatoire") String membreTokenId,
        @Positive(message = "Le numéro de lit doit être strictement positif") Integer numeroLit
) {}
