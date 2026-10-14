package com.pokedex.domain.model;

import java.time.Instant;
import java.util.Objects;

public record User(Long id, String username, String passwordHash, Role role, Instant createdAt) {

    public User {
        Objects.requireNonNull(username, "username");
        Objects.requireNonNull(passwordHash, "passwordHash");
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(createdAt, "createdAt");
    }

    public static User newUser(String username, String passwordHash, Role role, Instant now) {
        return new User(null, username, passwordHash, role, now);
    }

    @Override
    public String toString() {
        return "User[id=%s, username=%s, role=%s]".formatted(id, username, role);
    }
}
