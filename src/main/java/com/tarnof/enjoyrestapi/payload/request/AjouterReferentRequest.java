package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotBlank;

public record AjouterReferentRequest(
    @NotBlank(message = "Le tokenId du référent est obligatoire")
    String referentTokenId
) {}
