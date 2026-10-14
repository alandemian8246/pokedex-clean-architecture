package com.pokedex.domain.exception;

/**
 * Base type for every business-rule violation raised by the domain or application layers.
 * Adapters translate subclasses into protocol-specific responses (e.g. HTTP status codes).
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
