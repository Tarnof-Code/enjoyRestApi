package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.NiveauScolaire;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Date;

public record CreateEnfantRequest(
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    String nom,
    
    @NotEmpty(message = "Le champ prénom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    String prenom,
    
    @NotNull(message = "Le champ genre ne peut pas être vide.")
    Genre genre,
    
    @NotNull(message = "Le champ date de naissance ne peut pas être vide.")
    Date dateNaissance,
    
    @NotNull(message = "Le champ niveau scolaire ne peut pas être vide.")
    NiveauScolaire niveauScolaire
) {}
