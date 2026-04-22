package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.entities.Horaire;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SaveHoraireRequest(
        @NotBlank(message = "Le libellé d'horaire est obligatoire")
        @Pattern(
                regexp = Horaire.LIBELLE_HORAIRE_PATTERN,
                message =
                        "L'horaire doit être au format 6h00, 7h15, 18h30, etc. (h entre 0 et 23, minutes sur deux chiffres)")
        String libelle
) {}
