package com.tarnof.enjoyrestapi.enums;

public enum TypeGroupe {
    /** Groupe thématique : les enfants sont ajoutés manuellement */
    THEMATIQUE,
    /** Groupe par tranche d'âge : ageMin et ageMax obligatoires, ajout auto des enfants */
    AGE,
    /** Groupe par niveau scolaire : niveauScolaireMin et niveauScolaireMax obligatoires, ajout auto des enfants */
    NIVEAU_SCOLAIRE
}
