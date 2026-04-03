package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ReorderMomentsRequest(
        @NotEmpty(message = "La liste des identifiants de moments est obligatoire")
        List<@NotNull Integer> momentIds
) {}
