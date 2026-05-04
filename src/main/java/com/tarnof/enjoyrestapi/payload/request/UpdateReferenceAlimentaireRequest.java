package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateReferenceAlimentaireRequest(
        @NotBlank(message = "Le libellé est obligatoire") String libelle,
        Integer ordre,
        boolean actif
) {}
