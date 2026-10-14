package com.pokedex.domain.model;

import com.pokedex.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CredentialsTest {

    @Test
    void normalizesUsernameToLowercase() {
        assertThat(new Credentials(" Ash_Ketchum ", "Pikachu25").username()).isEqualTo("ash_ketchum");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab", "has space", "way_too_long_username_over_thirty", "bad-char"})
    void rejectsInvalidUsernames(String username) {
        assertThatThrownBy(() -> new Credentials(username, "Pikachu25"))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).errors()).containsKey("username"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"short1", "onlyletters", "12345678"})
    void rejectsWeakPasswords(String password) {
        assertThatThrownBy(() -> new Credentials("ash", password))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).errors()).containsKey("password"));
    }

    @Test
    void rejectsPasswordsLongerThanBcryptLimit() {
        assertThatThrownBy(() -> new Credentials("ash", "a1".repeat(37)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void neverPrintsThePassword() {
        assertThat(new Credentials("ash", "Pikachu25").toString()).doesNotContain("Pikachu25");
    }
}
