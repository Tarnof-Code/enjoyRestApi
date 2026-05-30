package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.TypeChambre;

public record ChambreOccupantDto(
        int id,
        TypeChambre typeOccupant,
        Integer enfantId,
        String membreTokenId,
        String nom,
        String prenom,
        Integer numeroLit
) {}
