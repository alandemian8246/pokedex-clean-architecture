package com.pokedex.application.auth;

import com.pokedex.application.port.PasswordHasher;
import com.pokedex.application.port.UserRepository;
import com.pokedex.domain.exception.ConflictException;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.Role;
import com.pokedex.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;

import static com.pokedex.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository users;
    @Mock
    private PasswordHasher hasher;

    private RegisterUserUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterUserUseCase(users, hasher, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void registersUserWithHashedPasswordAndUserRole() {
        when(users.existsByUsername("ash")).thenReturn(false);
        when(hasher.hash("Pikachu25")).thenReturn("hashed");
        when(users.save(any())).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            return new User(10L, user.username(), user.passwordHash(), user.role(), user.createdAt());
        });

        User user = useCase.execute("Ash", "Pikachu25");

        assertThat(user.id()).isEqualTo(10L);
        assertThat(user.username()).isEqualTo("ash");
        assertThat(user.passwordHash()).isEqualTo("hashed");
        assertThat(user.role()).isEqualTo(Role.USER);
        assertThat(user.createdAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsDuplicateUsername() {
        when(users.existsByUsername("ash")).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("ash", "Pikachu25")).isInstanceOf(ConflictException.class);
        verify(users, never()).save(any());
    }

    @Test
    void rejectsInvalidCredentialsBeforeTouchingStorage() {
        assertThatThrownBy(() -> useCase.execute("a", "weak")).isInstanceOf(ValidationException.class);
        verifyNoInteractions(users, hasher);
    }
}
