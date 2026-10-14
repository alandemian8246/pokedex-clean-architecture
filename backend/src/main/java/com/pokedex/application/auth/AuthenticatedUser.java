package com.pokedex.application.auth;

import com.pokedex.domain.model.AuthToken;
import com.pokedex.domain.model.User;

public record AuthenticatedUser(User user, AuthToken token) {
}
