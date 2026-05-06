package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.response.ReferencesAlimentairesAgregeesEnfantsDto;

public interface ReferencesAlimentairesAgregeesEnfantsService {

    /**
     * Agrège les allergènes et régimes/préférences distincts issus des dossiers des enfants inscrits au séjour.
     * L’appelant doit appartenir au séjour (directeur ou équipe, ou admin).
     */
    ReferencesAlimentairesAgregeesEnfantsDto agregerPourSejour(int sejourId, String utilisateurTokenId);
}
