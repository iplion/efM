package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class BlockRequestAlreadyExistsException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String MESSAGE = "Block request already exists";

    public BlockRequestAlreadyExistsException() {
        super(STATUS, MESSAGE);
    }
}
