package org.example.backend.exception;

/**
 * Excepción para errores relacionados con reglas de negocio
 * Por ejemplo, intentar reservar un asiento que ya está ocupado
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}