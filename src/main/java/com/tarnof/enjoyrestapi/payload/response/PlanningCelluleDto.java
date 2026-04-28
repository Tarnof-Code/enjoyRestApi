package com.tarnof.enjoyrestapi.payload.response;

import java.time.LocalDate;
import java.util.List;

public record PlanningCelluleDto(
        int id,
        LocalDate jour,
        List<String> membreTokenIds,
        List<Integer> horaireIds,
        List<String> horaireLibelles,
        List<Integer> momentIds,
        List<Integer> groupeIds,
        List<Integer> lieuIds,
        String texteLibre) {}
