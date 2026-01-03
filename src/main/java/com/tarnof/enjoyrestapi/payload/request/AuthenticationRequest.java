package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequest(
    @NotBlank(message = "L'email est obligatoire")
    String email,
    @NotBlank(message = "Le mot de passe est obligatoire")
    String motDePasse
) {}
