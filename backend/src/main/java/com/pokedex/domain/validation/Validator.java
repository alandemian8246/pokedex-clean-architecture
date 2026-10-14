package com.pokedex.domain.validation;

import com.pokedex.domain.exception.ValidationException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Collects every rule violation before failing, so callers get all field errors at once.
 */
public final class Validator {

    private final Map<String, String> errors = new LinkedHashMap<>();

    public Validator check(boolean valid, String field, String message) {
        if (!valid) {
            errors.putIfAbsent(field, message);
        }
        return this;
    }

    public void throwIfInvalid() {
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
