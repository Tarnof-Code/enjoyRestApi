package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveMomentRequest(
        @NotBlank(message = "Le nom du moment est obligatoire")
        @Size(max = 200)
        String nom
) {}
