package com.example.fund_app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler(OwnerAlreadyExistsException.class)
    public ResponseEntity<String> handleOwnerAlreadyExistsException(OwnerAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exception.getMessage());
    }


    @ExceptionHandler(DbRecordNotFoundException.class)
    public ResponseEntity<String> handleDbRecordNotFoundException(DbRecordNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

    @ExceptionHandler(OwnerActionInvalidException.class)
    public ResponseEntity<String> handleOwnerActionInvalidException(OwnerActionInvalidException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(AccountActionInvalidException.class)
    public ResponseEntity<String> handleAccountActionInvalidException(AccountActionInvalidException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
    }

    @ExceptionHandler(ExchangeRateNotRetrievableException.class)
    public ResponseEntity<String> handleExchangeRateNotRetrievableException(ExchangeRateNotRetrievableException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exception.getMessage());
    }
}
