package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.GenreChambre;
import com.tarnof.enjoyrestapi.enums.TypeChambre;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record SaveChambreRequest(
        @NotNull(message = "Le type de chambre est obligatoire")
        TypeChambre typeChambre,

        @NotBlank(message = "L'identifiant de la chambre est obligatoire")
        @Size(min = 1, max = 50)
        String identifiant,

        @Size(max = 150)
        String nom,

        @NotNull(message = "La capacité maximale est obligatoire")
        @Positive(message = "La capacité maximale doit être strictement positive")
        Integer capaciteMax,

        @NotNull(message = "Le genre autorisé est obligatoire")
        GenreChambre genreAutorise,

        @Size(max = 2000)
        String description,

        @Size(max = 100)
        String batiment,

        @Size(max = 100)
        String couloir,

        Integer etage
) {}
