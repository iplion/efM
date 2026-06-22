package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class BlockRequestAlreadyProcessedException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.BAD_REQUEST;
    private static final String MESSAGE = "Block request is already processed";

    public BlockRequestAlreadyProcessedException() {
        super(STATUS, MESSAGE);
    }
}
