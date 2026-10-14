package com.pokedex.domain.exception;

import java.util.Map;

/**
 * Raised when input violates a domain rule. Carries one message per offending field.
 */
public class ValidationException extends DomainException {

    private final transient Map<String, String> errors;

    public ValidationException(Map<String, String> errors) {
        super("Validation failed: " + errors);
        this.errors = Map.copyOf(errors);
    }

    public static ValidationException of(String field, String message) {
        return new ValidationException(Map.of(field, message));
    }

    public Map<String, String> errors() {
        return errors;
    }
}
