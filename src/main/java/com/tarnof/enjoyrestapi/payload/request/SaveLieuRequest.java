package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.EmplacementLieu;
import com.tarnof.enjoyrestapi.enums.UsageLieu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record SaveLieuRequest(
        @NotBlank(message = "Le nom du lieu est obligatoire")
        @Size(min = 1, max = 150)
        String nom,

        @NotNull(message = "L'emplacement est obligatoire")
        EmplacementLieu emplacement,

        @Positive(message = "Le nombre maximum doit être strictement positif")
        Integer nombreMax,

        boolean partageableEntreAnimateurs,

        Integer nombreMaxActivitesSimultanees,

        @NotEmpty(message = "Sélectionnez au moins un usage : activité, surveillance et/ou rassemblement")
        Set<UsageLieu> usages
) {}
