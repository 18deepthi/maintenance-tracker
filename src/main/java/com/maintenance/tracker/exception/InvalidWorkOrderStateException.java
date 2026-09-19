package com.maintenance.tracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidWorkOrderStateException extends RuntimeException {

    public InvalidWorkOrderStateException(String message) {
        super(message);
    }
}