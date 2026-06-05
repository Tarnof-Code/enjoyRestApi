package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record HistoriqueModificationChambreDto(
        @JsonUnwrapped HistoriqueModificationBaseDto base,
        Integer chambreId) {}
