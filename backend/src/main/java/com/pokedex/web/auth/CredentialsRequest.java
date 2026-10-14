package com.pokedex.web.auth;

import jakarta.validation.constraints.NotBlank;

public record CredentialsRequest(@NotBlank String username, @NotBlank String password) {

    @Override
    public String toString() {
        return "CredentialsRequest[username=%s, password=***]".formatted(username);
    }
}
