package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record ActivitePrestataireDto(
        int id,
        String nom,
        LocalDate date,
        List<MomentDto> moments,
        int sejourId,
        @JsonFormat(pattern = "HH:mm")
        LocalTime heureDepart,
        @JsonFormat(pattern = "HH:mm")
        LocalTime heureRetour,
        String informations,
        String telephone,
        List<Integer> groupeIds,
        List<NonParticipationPrestataireDto> nonParticipations
) {}
