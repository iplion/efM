package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class CardProcessingException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.INTERNAL_SERVER_ERROR;
    private static final String MESSAGE = "Card could not be processed.";

    public CardProcessingException(String message) {
        super(STATUS, MESSAGE + " " + message);
    }

    public CardProcessingException(Throwable cause) {
        super(STATUS, MESSAGE, cause);
    }

}
