package com.pokedex.application.auth;

import com.pokedex.application.port.UserRepository;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.model.User;

public class GetCurrentUserUseCase {

    private final UserRepository users;

    public GetCurrentUserUseCase(UserRepository users) {
        this.users = users;
    }

    public User execute(String username) {
        return users.findByUsername(username).orElseThrow(() -> new NotFoundException("User", username));
    }
}
