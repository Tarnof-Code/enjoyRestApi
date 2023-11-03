package com.tarnof.enjoyrestapi.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenException extends RuntimeException {
    public TokenException(String email, String message) {
        super(String.format("Failed for [%s]: %s", email, message));
    }
}
