package com.example.fund_app.exception;

public class ExchangeRateNotRetrievableException extends RuntimeException {

    public ExchangeRateNotRetrievableException(String message) {
        super(message);
    }
}
