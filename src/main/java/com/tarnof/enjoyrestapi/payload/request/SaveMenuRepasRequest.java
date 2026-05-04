package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.TypeRepas;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record SaveMenuRepasRequest(
        @NotNull(message = "La date du repas est obligatoire") LocalDate dateRepas,
        @NotNull(message = "Le type de repas est obligatoire") TypeRepas typeRepas,
        String detailPetitDejeunerOuGouter,
        String entree,
        String plat,
        String fromageOuEntremet,
        String dessert,
        List<Integer> allergeneIds,
        List<Integer> regimePreferenceIds
) {}
