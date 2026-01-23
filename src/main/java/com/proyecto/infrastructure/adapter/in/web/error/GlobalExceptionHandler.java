package com.proyecto.infrastructure.adapter.in.web.error;

import com.proyecto.domain.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Object> handleEmailAlreadyExistsException(EmailAlreadyExistsException ex, WebRequest request) {
        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now(),
                "status", HttpStatus.CONFLICT.value(),
                "error", "Conflict",
                "message", ex.getMessage(),
                "path", request.getDescription(false).substring(4)
        );
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }
}