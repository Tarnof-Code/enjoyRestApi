package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {

    @NotBlank(message = "Le tokenId est obligatoire")
    private String tokenId;

    // Obligatoire uniquement pour les utilisateurs non-admin
    private String ancienMotDePasse;

    @NotBlank(message = "Le nouveau mot de passe est obligatoire")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*!]).{4,}$",
            message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, un caractère spécial, et au moins 4 caractères"
    )
    private String nouveauMotDePasse;
}
