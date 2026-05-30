package com.tarnof.enjoyrestapi.utils;

import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.GenreChambre;

/** Règles de compatibilité genre occupant ↔ chambre (enfant ou membre d'équipe). */
public final class ChambreGenreRules {

    private ChambreGenreRules() {
    }

    public static boolean occupantCompatibleAvecChambre(Genre genreOccupant, GenreChambre genreChambre) {
        if (genreChambre == null || genreOccupant == null) {
            return false;
        }
        return switch (genreChambre) {
            case MIXTE -> true;
            case MASCULIN -> genreOccupant == Genre.Masculin;
            case FEMININ -> genreOccupant == Genre.Féminin;
        };
    }
}
