package com.tarnof.enjoyrestapi.payload.response;

import java.util.List;

/**
 * Une ligne pour l'écran sanitaire : identité, groupes du séjour, dossier (nullable si aucune ligne dossier).
 */
public record EnfantDossierSanitaireLigneDto(
        int enfantId,
        String prenom,
        String nom,
        List<GroupeResumeDto> groupes,
        DossierEnfantDto dossier) {}
