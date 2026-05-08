package com.tarnof.enjoyrestapi.payload.response;

/**
 * Représentation légère d'un groupe du séjour (ex. listes & filtres sanitaires).
 * {@code libelle} correspond au nom du groupe en base.
 */
public record GroupeResumeDto(int id, String libelle) {}
