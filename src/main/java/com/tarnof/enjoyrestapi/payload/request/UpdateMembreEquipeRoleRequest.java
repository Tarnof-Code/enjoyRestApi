package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.RoleSejour;
import jakarta.validation.constraints.NotNull;

public record UpdateMembreEquipeRoleRequest(
    @NotNull(message = "Le rôle de séjour est obligatoire")
    RoleSejour roleSejour
) {}
