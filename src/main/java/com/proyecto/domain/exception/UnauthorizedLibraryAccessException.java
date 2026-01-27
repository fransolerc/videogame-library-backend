package com.proyecto.domain.exception;

public class UnauthorizedLibraryAccessException extends RuntimeException {
    public UnauthorizedLibraryAccessException(String message) {
        super(message);
    }
}