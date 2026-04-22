package com.tarnof.enjoyrestapi.exceptions;

/**
 * Un animateur est déjà planifié sur une autre activité pour le même séjour, jour et moment.
 * Le code {@link #CODE} est exposé au client pour le traiter explicitement côté interface.
 */
public class ConflitPlanningAnimateurException extends RuntimeException {

    /** Clé stable pour le front (i18n, toasts, redirections) */
    public static final String CODE = "ANIMATEUR_DEJA_AFFECTE_CRENEAU";

    public ConflitPlanningAnimateurException(String message) {
        super(message);
    }
}
