package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
    @NotBlank(message = "Le tokenId est obligatoire")
    String tokenId,
    // Obligatoire uniquement pour les utilisateurs non-admin
    String ancienMotDePasse,
    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*!]).{4,}$",
        message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, un caractère spécial, et au moins 4 caractères"
    )
    String nouveauMotDePasse
) {}
