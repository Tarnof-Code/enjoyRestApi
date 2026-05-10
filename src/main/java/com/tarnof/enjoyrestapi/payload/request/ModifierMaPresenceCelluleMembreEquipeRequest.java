package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotNull;

public record ModifierMaPresenceCelluleMembreEquipeRequest(@NotNull Boolean present) {}
