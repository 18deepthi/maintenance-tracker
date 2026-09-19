package com.maintenance.tracker.exception;

import com.maintenance.tracker.dto.ErrorResponse;
import com.maintenance.tracker.dto.FieldErrorDto;
import jakarta.persistence.OptimisticLockException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<FieldErrorDto> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::mapFieldError)
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI(), fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Malformed JSON request or invalid field format.",
                request.getRequestURI(),
                Collections.emptyList()
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

        String paramName = ex.getName();
        Object value = ex.getValue();
        String message = "Invalid value '" + value + "' provided for parameter '" + paramName + "'.";

        return buildResponse(HttpStatus.BAD_REQUEST, message, request.getRequestURI(), Collections.emptyList());
    }

    @ExceptionHandler({InvalidSortFieldException.class, PropertyReferenceException.class})
    public ResponseEntity<ErrorResponse> handleSortExceptions(
            RuntimeException ex, HttpServletRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getRequestURI(), Collections.emptyList());
    }

    @ExceptionHandler({ResourceNotFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(
            Exception ex, HttpServletRequest request) {

        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI(), Collections.emptyList());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

        String message = "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.";
        return buildResponse(
                HttpStatus.METHOD_NOT_ALLOWED, message, request.getRequestURI(), Collections.emptyList()
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpServletRequest request) {

        String message = "Content-Type '" + ex.getContentType() + "' is not supported.";
        return buildResponse(
                HttpStatus.UNSUPPORTED_MEDIA_TYPE, message, request.getRequestURI(), Collections.emptyList()
        );
    }

    @ExceptionHandler(InvalidWorkOrderStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(
            InvalidWorkOrderStateException ex, HttpServletRequest request) {

        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getRequestURI(), Collections.emptyList());
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(
            Exception ex, HttpServletRequest request) {

        String message = "The record was updated by another transaction. Please reload and try again.";
        return buildResponse(HttpStatus.CONFLICT, message, request.getRequestURI(), Collections.emptyList());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        logger.error("Unhandled exception caught while processing request to {}: ", request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please contact support.",
                request.getRequestURI(),
                Collections.emptyList()
        );
    }

    private FieldErrorDto mapFieldError(FieldError err) {
        String field = err.getField();
        if (err.isBindingFailure()) {
            Object rejected = err.getRejectedValue();
            String message = (rejected != null)
                    ? "Invalid value '" + rejected + "' provided."
                    : "Invalid value provided.";
            return new FieldErrorDto(field, message);
        }
        return new FieldErrorDto(field, err.getDefaultMessage());
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String message, String path, List<FieldErrorDto> fieldErrors) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path,
                fieldErrors
        );
        return ResponseEntity.status(status).body(response);
    }
}