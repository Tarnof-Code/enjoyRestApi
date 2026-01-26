package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.NiveauScolaire;

import java.util.Date;

public record EnfantDto(
    int id,
    String nom,
    String prenom,
    Genre genre,
    Date dateNaissance,
    NiveauScolaire niveauScolaire
) {}
