package com.tarnof.enjoyrestapi.payload.response;

import java.time.LocalDate;
import java.util.List;

public record ActiviteDto(
        int id,
        LocalDate date,
        String nom,
        String description,
        int sejourId,
        LieuDto lieu,
        List<MembreEquipeInfo> membres,
        List<Integer> groupeIds,
        String avertissementLieu
) {
    public record MembreEquipeInfo(
            String tokenId,
            String nom,
            String prenom
    ) {}
}
