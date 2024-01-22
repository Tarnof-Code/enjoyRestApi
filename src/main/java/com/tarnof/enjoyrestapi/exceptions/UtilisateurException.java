package com.tarnof.enjoyrestapi.exceptions;

import jakarta.validation.ConstraintViolationException;

public class UtilisateurException extends RuntimeException {
    public UtilisateurException(String message) {
        super(message);
    }

    public UtilisateurException(String message, Throwable cause) {
        super(message, cause);
    }
}

