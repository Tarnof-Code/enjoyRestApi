package com.tarnof.enjoyrestapi.enums;

/** Rôles qu’un même lieu peut cumuler (au moins un obligatoire). */
public enum UsageLieu {
    /** Lieu où l’on affecte des activités (créneaux animés). */
    ACTIVITE,
    /** Lieu mentionné dans un planning de surveillance. */
    SURVEILLANCE,
    /** Lieu de rassemblement dans un planning. */
    RASSEMBLEMENT
}
