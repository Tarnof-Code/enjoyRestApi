package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveTypeActiviteRequest(
        @NotBlank(message = "Le libellé du type d'activité est obligatoire")
        @Size(max = 100)
        String libelle
) {}
