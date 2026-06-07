package com.tarnof.enjoyrestapi.exceptions;

/**
 * Un enfant est déjà planifié sur une autre activité pour le même séjour, jour et moment.
 * Le code {@link #CODE} est exposé au client pour le traiter explicitement côté interface.
 */
public class ConflitPlanningEnfantException extends RuntimeException {

    /** Clé stable pour le front (i18n, toasts, redirections) */
    public static final String CODE = "ENFANT_DEJA_AFFECTE_CRENEAU";

    public ConflitPlanningEnfantException(String message) {
        super(message);
    }
}
