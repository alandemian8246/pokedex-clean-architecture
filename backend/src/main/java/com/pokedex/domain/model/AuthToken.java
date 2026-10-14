package com.pokedex.domain.model;

import java.time.Instant;

public record AuthToken(String value, Instant expiresAt) {

    @Override
    public String toString() {
        return "AuthToken[expiresAt=%s]".formatted(expiresAt);
    }
}
