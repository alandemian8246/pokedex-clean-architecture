package com.pokedex.application.auth;

import com.pokedex.application.port.PasswordHasher;
import com.pokedex.application.port.TokenIssuer;
import com.pokedex.application.port.UserRepository;
import com.pokedex.domain.exception.InvalidCredentialsException;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.model.AuthToken;
import com.pokedex.domain.model.Role;
import com.pokedex.domain.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.pokedex.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginUseCaseTest {

    private static final User ASH = new User(1L, "ash", "hashed", Role.USER, NOW);

    @Mock
    private UserRepository users;
    @Mock
    private PasswordHasher hasher;
    @Mock
    private TokenIssuer tokenIssuer;

    @InjectMocks
    private LoginUseCase login;

    @InjectMocks
    private GetCurrentUserUseCase currentUser;

    @Test
    void issuesTokenForValidCredentials() {
        AuthToken token = new AuthToken("jwt", NOW.plusSeconds(60));
        when(users.findByUsername("ash")).thenReturn(Optional.of(ASH));
        when(hasher.matches("Pikachu25", "hashed")).thenReturn(true);
        when(tokenIssuer.issue(ASH)).thenReturn(token);

        AuthenticatedUser result = login.execute(" ASH ", "Pikachu25");

        assertThat(result.user()).isEqualTo(ASH);
        assertThat(result.token()).isEqualTo(token);
    }

    @Test
    void rejectsWrongPassword() {
        when(users.findByUsername("ash")).thenReturn(Optional.of(ASH));
        when(hasher.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> login.execute("ash", "wrong")).isInstanceOf(InvalidCredentialsException.class);
        verify(tokenIssuer, never()).issue(any());
    }

    @Test
    void rejectsUnknownUserWithTheSameError() {
        when(users.findByUsername("gary")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> login.execute("gary", "Eevee1234"))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessage("Invalid username or password");
    }

    @ParameterizedTest
    @CsvSource(value = {"NULL, pw", "' ', pw", "ash, NULL", "ash, ''"}, nullValues = "NULL")
    void rejectsMissingCredentials(String username, String password) {
        assertThatThrownBy(() -> login.execute(username, password)).isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void currentUserLooksUpByUsername() {
        when(users.findByUsername("ash")).thenReturn(Optional.of(ASH));

        assertThat(currentUser.execute("ash")).isEqualTo(ASH);
    }

    @Test
    void currentUserThrowsWhenAccountWasRemoved() {
        when(users.findByUsername("ash")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> currentUser.execute("ash")).isInstanceOf(NotFoundException.class);
    }
}
