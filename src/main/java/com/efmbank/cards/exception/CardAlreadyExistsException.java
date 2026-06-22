package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class CardAlreadyExistsException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String MESSAGE = "Card already exists";

    public CardAlreadyExistsException() {
        super(STATUS, MESSAGE);
    }
}
