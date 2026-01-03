package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.RoleSejour;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MembreEquipeRequest(
    @NotBlank(message = "Le tokenId est obligatoire")
    String tokenId,
    @NotNull(message = "Le rôle de séjour est obligatoire")
    RoleSejour roleSejour
) {}
