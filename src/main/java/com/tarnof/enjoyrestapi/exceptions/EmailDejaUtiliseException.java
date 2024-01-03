package com.tarnof.enjoyrestapi.exceptions;

public class EmailDejaUtiliseException extends RuntimeException {

    public EmailDejaUtiliseException(String message) {
        super(message);
    }

    public EmailDejaUtiliseException(String message, Throwable cause) {
        super(message, cause);
    }
}
