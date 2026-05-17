package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record HistoriqueModificationCahierInfirmerieDto(
        @JsonUnwrapped HistoriqueModificationBaseDto base, Integer cahierInfirmerieEntreeId) {}
