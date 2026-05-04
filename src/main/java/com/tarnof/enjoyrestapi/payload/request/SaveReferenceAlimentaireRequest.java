package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.TypeReferenceAlimentaire;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SaveReferenceAlimentaireRequest(
        @NotNull(message = "Le type est obligatoire") TypeReferenceAlimentaire type,
        @NotBlank(message = "Le libellé est obligatoire") String libelle,
        Integer ordre
) {}
