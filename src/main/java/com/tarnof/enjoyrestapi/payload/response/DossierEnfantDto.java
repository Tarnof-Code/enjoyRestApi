package com.tarnof.enjoyrestapi.payload.response;

public record DossierEnfantDto(
    int id,
    int enfantId,
    String emailParent1,
    String telephoneParent1,
    String emailParent2,
    String telephoneParent2,
    String informationsMedicales,
    String pai,
    String informationsAlimentaires,
    String traitementMatin,
    String traitementMidi,
    String traitementSoir,
    String traitementSiBesoin,
    String autresInformations,
    String aPrendreEnSortie
) {}
