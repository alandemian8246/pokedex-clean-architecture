package com.pokedex.domain.exception;

public class NotFoundException extends DomainException {

    public NotFoundException(String resource, Object identifier) {
        super("%s '%s' was not found".formatted(resource, identifier));
    }
}
