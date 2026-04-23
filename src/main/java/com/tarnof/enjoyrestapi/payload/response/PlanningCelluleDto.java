package com.tarnof.enjoyrestapi.payload.response;

import java.time.LocalDate;
import java.util.List;

public record PlanningCelluleDto(
        int id,
        LocalDate jour,
        List<String> membreTokenIds,
        Integer horaireId,
        String horaireLibelle,
        Integer momentId,
        Integer groupeId,
        Integer lieuId,
        String texteLibre) {}
