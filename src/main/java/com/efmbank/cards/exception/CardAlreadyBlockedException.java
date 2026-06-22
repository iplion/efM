package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class CardAlreadyBlockedException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String MESSAGE = "Card is already blocked";

    public CardAlreadyBlockedException() {
        super(STATUS, MESSAGE);
    }
}
