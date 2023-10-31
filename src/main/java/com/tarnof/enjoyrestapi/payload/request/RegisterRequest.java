package com.tarnof.enjoyrestapi.payload.request;


import com.tarnof.enjoyrestapi.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String telephone;
    @NotBlank(message = "Ce champs est obligatoire")
    @Email(message = "email format is not valid")
    private String email;
    @NotBlank(message = "Ce champs est obligatoire")
    private String motDePasse;
    @NotNull
    private Role role;
}
