package com.tarnof.enjoyrestapi.payload.response;

import java.time.Instant;

public record PlanningGrilleSummaryDto(int id, int sejourId, String titre, Instant miseAJour) {}
