package com.tarnof.enjoyrestapi.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.tarnof.enjoyrestapi.payload.response.NonParticipationPrestataireDto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SaveActivitePrestataireRequest(
        @NotBlank(message = "Le nom est obligatoire")
        @Size(max = 200)
        String nom,

        @NotNull(message = "La date est obligatoire")
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        @NotEmpty(message = "Au moins un moment est requis")
        List<@NotNull(message = "Identifiant de moment invalide") Integer> momentIds,

        @JsonFormat(pattern = "HH:mm")
        LocalTime heureDepart,

        @JsonFormat(pattern = "HH:mm")
        LocalTime heureRetour,

        @Size(max = 5000)
        String informations,

        @Size(max = 30)
        String telephone,

        List<Integer> groupeIds,

        /**
         * Liste complète de remplacement si fournie. Omise ({@code null}) en modification : conserver puis élaguer
         * selon les nouveaux moments / groupes.
         */
        List<NonParticipationPrestataireDto> nonParticipations
) {}
