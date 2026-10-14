package com.pokedex.web.auth;

import com.pokedex.domain.model.User;

import java.time.Instant;

public record UserResponse(Long id, String username, String role, Instant createdAt) {

    static UserResponse from(User user) {
        return new UserResponse(user.id(), user.username(), user.role().name(), user.createdAt());
    }
}
