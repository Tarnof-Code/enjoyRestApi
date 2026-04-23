package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpsertPlanningCellulesRequest(
        @NotNull(message = "La liste des cellules est obligatoire") @Valid List<PlanningCellulePayload> cellules) {}
