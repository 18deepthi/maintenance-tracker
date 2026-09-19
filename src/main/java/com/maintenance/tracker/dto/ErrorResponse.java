package com.maintenance.tracker.dto;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final List<FieldErrorDto> fieldErrors;

    public ErrorResponse(Instant timestamp, int status, String error, String message,
                         String path, List<FieldErrorDto> fieldErrors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.fieldErrors = (fieldErrors != null) ? fieldErrors : Collections.emptyList();
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public String getPath() {
        return path;
    }

    public List<FieldErrorDto> getFieldErrors() {
        return fieldErrors;
    }
}