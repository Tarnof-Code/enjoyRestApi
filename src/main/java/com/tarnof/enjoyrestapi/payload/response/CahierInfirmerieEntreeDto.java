package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.TypeAppelInfirmerie;
import com.tarnof.enjoyrestapi.enums.TypeSoinInfirmerie;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record CahierInfirmerieEntreeDto(
        int id,
        int sejourId,
        int enfantId,
        String enfantNom,
        String enfantPrenom,
        String createurTokenId,
        String createurNom,
        String createurPrenom,
        Instant dateHeure,
        String description,
        String localisationCorps,
        Set<TypeSoinInfirmerie> soins,
        String soinsAutrePrecision,
        BigDecimal temperatureCelsius,
        Set<TypeAppelInfirmerie> appels,
        String appelAutrePrecision,
        String soigneurTokenId,
        String soigneurNom,
        String soigneurPrenom) {}
