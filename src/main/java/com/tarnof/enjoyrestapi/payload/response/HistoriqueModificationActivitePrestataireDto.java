package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record HistoriqueModificationActivitePrestataireDto(
        @JsonUnwrapped HistoriqueModificationBaseDto base, Integer activitePrestataireId) {}
