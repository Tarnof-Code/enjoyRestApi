package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AffecterOccupantsEquipeRequest(
        @NotEmpty(message = "Au moins un membre d'équipe doit être sélectionné")
        List<@Valid AffecterOccupantEquipeItemRequest> occupants
) {}
