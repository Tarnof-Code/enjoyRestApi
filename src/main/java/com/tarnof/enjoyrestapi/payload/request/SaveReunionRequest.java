package com.tarnof.enjoyrestapi.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SaveReunionRequest(
        @NotNull(message = "La date est obligatoire")
                @JsonFormat(pattern = "yyyy-MM-dd")
                LocalDate date,
        @Size(max = 500) String ordreDuJour,
        @NotNull(message = "Le contenu est obligatoire") JsonNode contenu
) {}
