package com.maintenance.tracker.exception;

public class InvalidWorkOrderStateException extends RuntimeException {

    public InvalidWorkOrderStateException(String message) {
        super(message);
    }
}