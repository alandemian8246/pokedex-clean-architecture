package com.pokedex.domain.exception;

/**
 * Raised when an upstream dependency (e.g. PokeAPI) is unavailable or returns an unexpected response.
 */
public class ExternalServiceException extends DomainException {

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
