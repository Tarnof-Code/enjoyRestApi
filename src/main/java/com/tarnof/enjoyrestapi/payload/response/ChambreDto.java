package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.GenreChambre;
import com.tarnof.enjoyrestapi.enums.TypeChambre;

import java.util.List;

public record ChambreDto(
        int id,
        int sejourId,
        TypeChambre typeChambre,
        String identifiant,
        String nom,
        int capaciteMax,
        GenreChambre genreAutorise,
        String description,
        String batiment,
        String couloir,
        Integer etage,
        GroupeResumeDto groupe,
        List<ReferentInfos> referents,
        List<ChambreOccupantDto> occupants
) {
    public record ReferentInfos(
            String tokenId,
            String nom,
            String prenom
    ) {}
}
