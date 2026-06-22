package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.NOT_FOUND;
    private static final String MESSAGE = "User not found";

    public UserNotFoundException() {
        super(STATUS, MESSAGE);
    }
}
