package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.time.LocalDate;

public record HistoriqueModificationPlanningCelluleDto(
        @JsonUnwrapped HistoriqueModificationBaseDto base,
        Integer planningLigneId,
        LocalDate planningJour,
        Integer planningCelluleId) {}
