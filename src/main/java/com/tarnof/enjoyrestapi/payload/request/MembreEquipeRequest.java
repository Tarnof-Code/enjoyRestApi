package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.RoleSejour;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembreEquipeRequest {
    private String tokenId;
    @NotNull(message = "Le rôle de séjour est obligatoire")
    @Enumerated(EnumType.STRING)
    private RoleSejour roleSejour;
}

