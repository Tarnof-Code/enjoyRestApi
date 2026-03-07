package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.Pattern;

public record UpdateDossierEnfantRequest(
    @Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email parent 1 non valide")
    String emailParent1,
    @Pattern(regexp = "^$|^(\\+33|0033|0)[1-9]([\\s.\\-]?[0-9]{2}){4}$", message = "Numéro de téléphone parent 1 non valide")
    String telephoneParent1,
    @Pattern(regexp = "^$|^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email parent 2 non valide")
    String emailParent2,
    @Pattern(regexp = "^$|^(\\+33|0033|0)[1-9]([\\s.\\-]?[0-9]{2}){4}$", message = "Numéro de téléphone parent 2 non valide")
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
