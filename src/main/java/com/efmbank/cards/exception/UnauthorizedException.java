package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.UNAUTHORIZED;
    private static final String MESSAGE = "Unauthorized";

    public UnauthorizedException() {
        super(STATUS, MESSAGE);
    }
}
