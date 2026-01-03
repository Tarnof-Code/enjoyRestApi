package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.RoleSejour;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.Instant;
import java.util.Date;

public record RegisterRequest(
    @NotBlank(message = "Ce champs est obligatoire")
    String prenom,
    @NotBlank(message = "Ce champs est obligatoire")
    String nom,
    @NotBlank(message = "Ce champs est obligatoire")
    String genre,
    @NotNull(message = "Ce champs est obligatoire")
    Date dateNaissance,
    @NotBlank(message = "Ce champs est obligatoire")
    @Pattern(regexp = "^0[0-9]{9}$", message = "N° de téléphone non valide")
    String telephone,
    @NotBlank(message = "Ce champs est obligatoire")
    @Email(message = "Email non valide")
    String email,
    @NotBlank(message = "Ce champs est obligatoire")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*!]).{4,}$", message = "* Le mot de passe doit contenir au moins une minuscule, une majuscule, et un caractère spécial, et comporter au moins 4 caractères")
    String motDePasse,
    Instant dateExpiration,
    @NotNull
    Role role,
    RoleSejour roleSejour
) {}
