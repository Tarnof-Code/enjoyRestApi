package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.RoleSejour;

import java.time.Instant;
import java.util.Date;

public record ProfilDto(
    String tokenId,
    Role role,
    RoleSejour roleSejour,
    String nom,
    String prenom,
    Genre genre,
    String email,
    String telephone,
    Date dateNaissance,
    Instant dateExpirationCompte
) {}
