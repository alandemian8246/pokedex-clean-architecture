package com.pokedex.web.auth;

import java.time.Instant;

public record TokenResponse(String accessToken, String tokenType, Instant expiresAt, UserResponse user) {
}
