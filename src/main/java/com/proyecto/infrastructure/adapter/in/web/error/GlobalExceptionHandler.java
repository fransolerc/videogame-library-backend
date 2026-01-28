package com.proyecto.infrastructure.adapter.in.web.error;

import com.proyecto.domain.exception.EmailAlreadyExistsException;
import com.proyecto.domain.exception.UnauthorizedLibraryAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String PATH = "path";

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {
        Map<String, Object> body = Map.of(
                TIMESTAMP, LocalDateTime.now(),
                STATUS, HttpStatus.CONFLICT.value(),
                ERROR, "Conflict",
                MESSAGE, ex.getMessage(),
                PATH, request.getDescription(false).substring(4)
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(UnauthorizedLibraryAccessException.class)
    public ResponseEntity<Object> handleUnauthorizedLibraryAccessException(UnauthorizedLibraryAccessException ex, WebRequest request) {
        Map<String, Object> body = Map.of(
                TIMESTAMP, LocalDateTime.now(),
                STATUS, HttpStatus.FORBIDDEN.value(),
                ERROR, "Forbidden",
                MESSAGE, ex.getMessage(),
                PATH, request.getDescription(false).substring(4)
        );
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> body = Map.of(
                TIMESTAMP, LocalDateTime.now(),
                STATUS, HttpStatus.BAD_REQUEST.value(),
                ERROR, "Bad Request",
                MESSAGE, "Validation failed",
                "errors", errors,
                PATH, request.getDescription(false).substring(4)
        );
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
