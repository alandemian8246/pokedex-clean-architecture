package com.pokedex.application.auth;

import com.pokedex.application.port.PasswordHasher;
import com.pokedex.application.port.TokenIssuer;
import com.pokedex.application.port.UserRepository;
import com.pokedex.domain.exception.InvalidCredentialsException;
import com.pokedex.domain.model.User;

import java.util.Locale;

/**
 * Authenticates a user and issues an access token. Unknown users and wrong passwords produce the same error
 * so the API does not reveal which usernames exist.
 */
public class LoginUseCase {

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final TokenIssuer tokenIssuer;

    public LoginUseCase(UserRepository users, PasswordHasher hasher, TokenIssuer tokenIssuer) {
        this.users = users;
        this.hasher = hasher;
        this.tokenIssuer = tokenIssuer;
    }

    public AuthenticatedUser execute(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            throw new InvalidCredentialsException();
        }
        User user = users.findByUsername(username.strip().toLowerCase(Locale.ROOT))
                .filter(found -> hasher.matches(password, found.passwordHash()))
                .orElseThrow(InvalidCredentialsException::new);
        return new AuthenticatedUser(user, tokenIssuer.issue(user));
    }
}
