package com.example.fund_app.exception;

public class DbRecordNotFoundException extends RuntimeException {

    public DbRecordNotFoundException(String message) {
        super(message);
    }
}
