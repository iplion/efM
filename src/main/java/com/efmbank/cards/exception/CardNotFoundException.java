package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class CardNotFoundException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "Card not found";

    public CardNotFoundException() {
        super(STATUS, MESSAGE);
    }
}
