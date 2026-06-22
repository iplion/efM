package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class BlockRequestNotFoundException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "Block request not found";

    public BlockRequestNotFoundException() {
        super(STATUS, MESSAGE);
    }
}
