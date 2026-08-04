package com.proyecto.domain.exception;

/**
 * Superclase para excepciones de dominio que representan un recurso no encontrado.
 * Permite un único manejo HTTP (404) en GlobalExceptionHandler mientras cada punto de
 * lanzamiento sigue usando un tipo concreto y descriptivo.
 */
public abstract class NotFoundException extends RuntimeException {
    protected NotFoundException(String message) {
        super(message);
    }
}
