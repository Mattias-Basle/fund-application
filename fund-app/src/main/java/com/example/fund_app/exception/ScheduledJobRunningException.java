package com.example.fund_app.exception;

public class ScheduledJobRunningException extends RuntimeException {

    public ScheduledJobRunningException(String message) {
        super(message);
    }
}
