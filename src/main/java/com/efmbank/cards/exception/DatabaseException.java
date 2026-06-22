package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class DatabaseException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    public DatabaseException(String message) {
        super(STATUS, message);
    }
}
