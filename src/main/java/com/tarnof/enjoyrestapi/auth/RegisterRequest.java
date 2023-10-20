package com.tarnof.enjoyrestapi.auth;


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
    private String prenom;
    private String nom;
    private String genre;
    private Date dateNaissance;
    private String telephone;
    private String email;
    private String motDePasse;
}
