package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.TypeAppelInfirmerie;
import com.tarnof.enjoyrestapi.enums.TypeSoinInfirmerie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

public record SaveCahierInfirmerieEntreeRequest(
        @NotNull(message = "La date et l'heure sont obligatoires") Instant dateHeure,
        @NotNull(message = "L'enfant est obligatoire") Integer enfantId,
        @NotBlank(message = "La description est obligatoire") String description,
        String localisationCorps,
        @NotEmpty(message = "Au moins un soin doit être renseigné") Set<TypeSoinInfirmerie> soins,
        String soinsAutrePrecision,
        // °C, obligatoire si « PRISE_TEMPERATURE » ∈ soins ; sinon null / absent en JSON
        BigDecimal temperatureCelsius,
        Set<TypeAppelInfirmerie> appels,
        String appelAutrePrecision,
        @NotBlank(message = "Le soigneur est obligatoire (tokenId du membre d'équipe)") String soigneurTokenId
) {
    public SaveCahierInfirmerieEntreeRequest {
        appels = appels == null ? Set.of() : appels;
    }
}
