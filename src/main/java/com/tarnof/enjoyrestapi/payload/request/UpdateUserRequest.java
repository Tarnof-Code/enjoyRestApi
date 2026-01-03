package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.Role;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.Date;

public record UpdateUserRequest(
    String tokenId,
    @NotEmpty(message = "Le champ prénom ne peut pas être vide.")
    String prenom,
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    String nom,
    @NotEmpty(message = "Le champ genre ne peut pas être vide.")
    String genre,
    @NotEmpty(message = "Le champ email ne peut pas être vide.")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email non valide")
    String email,
    @NotEmpty(message = "Le champ N° de téléphone ne peut pas être vide.")
    @Pattern(regexp = "^0[0-9]{9}$", message = "N° de téléphone non valide")
    String telephone,
    @NotNull(message = "Le champ date de naissance ne peut pas être vide.")
    Date dateNaissance,
    Role role,
    Instant dateExpirationCompte
) {}
