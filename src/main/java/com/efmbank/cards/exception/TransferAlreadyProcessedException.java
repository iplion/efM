package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class TransferAlreadyProcessedException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String MESSAGE = "Transfer already processed";

    public TransferAlreadyProcessedException() {
        super(STATUS, MESSAGE);
    }
}
