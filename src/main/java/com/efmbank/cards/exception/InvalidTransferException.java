package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class InvalidTransferException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;

    public InvalidTransferException(String message) {
        super(STATUS, message);
    }
}
