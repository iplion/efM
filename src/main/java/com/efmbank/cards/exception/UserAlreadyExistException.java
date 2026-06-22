package com.efmbank.cards.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistException extends ApiException {
    private static final HttpStatus STATUS = HttpStatus.CONFLICT;
    private static final String MESSAGE = "User already exists";

    public UserAlreadyExistException() {
        super(STATUS, MESSAGE);
    }
}
