package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateActiviteRequest(
        @NotNull(message = "La date est obligatoire")
        LocalDate date,

        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 200)
        String nom,

        @Size(max = 5000)
        String description,

        Integer lieuId,

        @NotEmpty(message = "Au moins un membre d'équipe est requis")
        List<@NotBlank(message = "Identifiant membre invalide") String> membreTokenIds,

        @NotEmpty(message = "Au moins un groupe est requis")
        List<@NotNull(message = "Identifiant de groupe invalide") Integer> groupeIds
) {}
