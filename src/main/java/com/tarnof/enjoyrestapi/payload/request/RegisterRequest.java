package com.tarnof.enjoyrestapi.payload.request;


import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.RoleSejour;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Ce champs est obligatoire")
    private String prenom;
    @NotBlank(message = "Ce champs est obligatoire")
    private String nom;
    @NotBlank(message = "Ce champs est obligatoire")
    private String genre;
    @NotNull(message = "Ce champs est obligatoire")
    private Date dateNaissance;
    @NotBlank(message = "Ce champs est obligatoire")
    @Pattern(regexp = "^0[0-9]{9}$", message = "N° de téléphone non valide")
    private String telephone;
    @NotBlank(message = "Ce champs est obligatoire")
    @Email(message = "Email non valide")
    private String email;
    @NotBlank(message = "Ce champs est obligatoire")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*!]).{4,}$", message = "* Le mot de passe doit contenir au moins une minuscule, une majuscule, et un caractère spécial, et comporter au moins 4 caractères")
    private String motDePasse;
    private Instant dateExpiration;
    @NotNull
    private Role role;
    private RoleSejour roleSejour;
}
