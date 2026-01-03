package com.tarnof.enjoyrestapi.payload.response;

import java.util.Date;
import java.util.List;

public record SejourDto(
    int id,
    String nom,
    String description,
    Date dateDebut,
    Date dateFin,
    String lieuDuSejour,
    DirecteurInfos directeur,
    List<ProfilDto> equipe
) {
    public record DirecteurInfos(
        String tokenId,
        String nom,
        String prenom
    ) {}
}
