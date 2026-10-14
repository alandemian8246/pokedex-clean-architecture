package com.pokedex.application.auth;

import com.pokedex.application.port.PasswordHasher;
import com.pokedex.application.port.UserRepository;
import com.pokedex.domain.exception.ConflictException;
import com.pokedex.domain.model.Credentials;
import com.pokedex.domain.model.Role;
import com.pokedex.domain.model.User;

import java.time.Clock;

public class RegisterUserUseCase {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final Clock clock;

    public RegisterUserUseCase(UserRepository users, PasswordHasher hasher, Clock clock) {
        this.users = users;
        this.hasher = hasher;
        this.clock = clock;
    }

    public User execute(String username, String password) {
        return register(new Credentials(username, password), Role.USER);
    }

    /** Used by the demo-data seeder to create privileged accounts; not exposed over HTTP. */
    public User register(Credentials credentials, Role role) {
        if (users.existsByUsername(credentials.username())) {
            throw new ConflictException("Username '%s' is already taken".formatted(credentials.username()));
        }
        String hash = hasher.hash(credentials.password());
        return users.save(User.newUser(credentials.username(), hash, role, clock.instant()));
    }
}
