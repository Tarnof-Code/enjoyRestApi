package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.TypeReferenceAlimentaire;

public record ReferenceAlimentaireDto(
        int id,
        TypeReferenceAlimentaire type,
        String libelle,
        Integer ordre,
        boolean actif
) {}
