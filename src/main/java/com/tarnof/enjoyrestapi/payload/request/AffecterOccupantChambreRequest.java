package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.Positive;

public record AffecterOccupantChambreRequest(
        @Positive(message = "Le numéro de lit doit être strictement positif")
        Integer numeroLit
) {}
