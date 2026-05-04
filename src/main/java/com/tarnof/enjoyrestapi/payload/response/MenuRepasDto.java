package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.TypeRepas;

import java.time.LocalDate;
import java.util.List;

public record MenuRepasDto(
        int id,
        int sejourId,
        LocalDate dateRepas,
        TypeRepas typeRepas,
        String detailPetitDejeunerOuGouter,
        String entree,
        String plat,
        String fromageOuEntremet,
        String dessert,
        List<ReferenceAlimentaireDto> allergenes,
        List<ReferenceAlimentaireDto> regimesEtPreferences
) {}
