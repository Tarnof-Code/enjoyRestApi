package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.EmplacementLieu;
import com.tarnof.enjoyrestapi.enums.UsageLieu;

import java.util.Set;

public record LieuDto(
        int id,
        String nom,
        EmplacementLieu emplacement,
        Integer nombreMax,
        boolean partageableEntreAnimateurs,
        Integer nombreMaxActivitesSimultanees,
        Set<UsageLieu> usages,
        int sejourId
) {}
