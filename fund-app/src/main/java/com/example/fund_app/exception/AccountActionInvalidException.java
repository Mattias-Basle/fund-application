package com.example.fund_app.exception;

public class AccountActionInvalidException extends RuntimeException {

    public AccountActionInvalidException(String message) {
        super(message);
    }
}
