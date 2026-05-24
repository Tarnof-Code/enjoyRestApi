package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDate;

public record ReunionDto(
        int id,
        int sejourId,
        @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,
        String ordreDuJour,
        JsonNode contenu
) {}
