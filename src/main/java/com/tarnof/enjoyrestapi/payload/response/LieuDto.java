package com.tarnof.enjoyrestapi.payload.response;

import com.tarnof.enjoyrestapi.enums.EmplacementLieu;

public record LieuDto(
        int id,
        String nom,
        EmplacementLieu emplacement,
        Integer nombreMax,
        int sejourId
) {}
