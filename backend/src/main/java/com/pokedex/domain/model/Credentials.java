package com.pokedex.domain.model;

import com.pokedex.domain.validation.Validator;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Registration credentials. Usernames are case-insensitive and stored lowercase.
 * Passwords are capped at 72 characters, the input limit of BCrypt.
 */
public record Credentials(String username, String password) {

    public static final int MIN_PASSWORD = 8;
    public static final int MAX_PASSWORD = 72;
    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_]{3,30}$");

    public Credentials {
        username = username == null ? "" : username.strip().toLowerCase(Locale.ROOT);
        password = password == null ? "" : password;

        new Validator()
                .check(USERNAME.matcher(username).matches(),
                        "username", "must be 3-30 characters: letters, digits or underscore")
                .check(password.length() >= MIN_PASSWORD && password.length() <= MAX_PASSWORD,
                        "password", "must be between " + MIN_PASSWORD + " and " + MAX_PASSWORD + " characters")
                .check(password.chars().anyMatch(Character::isLetter) && password.chars().anyMatch(Character::isDigit),
                        "password", "must contain at least one letter and one digit")
                .throwIfInvalid();
    }

    @Override
    public String toString() {
        return "Credentials[username=%s, password=***]".formatted(username);
    }
}
