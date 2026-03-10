package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.NiveauScolaire;
import com.tarnof.enjoyrestapi.enums.TypeGroupe;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateGroupeRequest(
    @NotBlank(message = "Le nom du groupe est obligatoire")
    @Size(min = 2, max = 100)
    String nom,

    @Size(max = 500)
    String description,

    @NotNull(message = "Le type de groupe est obligatoire")
    TypeGroupe typeGroupe,

    Integer ageMin,
    Integer ageMax,
    NiveauScolaire niveauScolaireMin,
    NiveauScolaire niveauScolaireMax
) {}
