package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AffecterOccupantsEnfantsRequest(
        @NotEmpty(message = "Au moins un enfant doit être sélectionné")
        List<@Valid AffecterOccupantEnfantItemRequest> occupants
) {}
