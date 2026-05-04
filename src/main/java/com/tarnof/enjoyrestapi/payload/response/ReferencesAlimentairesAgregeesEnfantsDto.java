package com.tarnof.enjoyrestapi.payload.response;

import java.util.List;

/**
 * Union des références alimentaires (allergènes / régimes) présentes sur au moins un dossier enfant du séjour.
 */
public record ReferencesAlimentairesAgregeesEnfantsDto(
        List<ReferenceAlimentaireDto> allergenes,
        List<ReferenceAlimentaireDto> regimesEtPreferences
) {}
