package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.response.ReferencesAlimentairesAgregeesEnfantsDto;

public interface ReferencesAlimentairesAgregeesEnfantsService {

    /** Agrège les allergènes et régimes/préférences distincts issus des dossiers des enfants inscrits au séjour. */
    ReferencesAlimentairesAgregeesEnfantsDto agregerPourSejour(int sejourId);
}
