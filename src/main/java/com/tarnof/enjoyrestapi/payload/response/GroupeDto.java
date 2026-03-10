package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.NiveauScolaire;
import com.tarnof.enjoyrestapi.enums.TypeGroupe;

import java.util.List;

public record GroupeDto(
    int id,
    String nom,
    String description,
    TypeGroupe typeGroupe,
    Integer ageMin,
    Integer ageMax,
    NiveauScolaire niveauScolaireMin,
    NiveauScolaire niveauScolaireMax,
    int sejourId,
    List<EnfantDto> enfants,
    List<ReferentInfos> referents
) {
    public record ReferentInfos(
        String tokenId,
        String nom,
        String prenom
    ) {}
}
