package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record HistoriqueModificationActiviteDto(
        @JsonUnwrapped HistoriqueModificationBaseDto base,
        Integer activiteId) {}
