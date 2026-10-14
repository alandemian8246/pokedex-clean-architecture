package com.pokedex.application.localpokemon;

import com.pokedex.TestData;
import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.domain.exception.ConflictException;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.LocalPokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.Set;

import static com.pokedex.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateLocalPokemonUseCaseTest {

    @Mock
    private LocalPokemonRepository repository;

    private UpdateLocalPokemonUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new UpdateLocalPokemonUseCase(repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void updatesCustomFields() {
        when(repository.findById(1)).thenReturn(Optional.of(TestData.localBulbasaur(2L)));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalPokemon updated = useCase.execute(new UpdateLocalPokemonCommand(1, "Bisasam", "Kanto",
                Set.of("Starter"), "German name", 2L));

        assertThat(updated.customFields().localizedName()).isEqualTo("Bisasam");
        assertThat(updated.customFields().tags()).containsExactly("starter");
        assertThat(updated.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void acceptsUpdateWithoutVersion() {
        when(repository.findById(1)).thenReturn(Optional.of(TestData.localBulbasaur(2L)));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalPokemon updated = useCase.execute(new UpdateLocalPokemonCommand(1, null, null, null, null, null));

        assertThat(updated.customFields().localizedName()).isNull();
    }

    @Test
    void rejectsInvalidPayloadBeforeLookingUpTheRecord() {
        UpdateLocalPokemonCommand command = new UpdateLocalPokemonCommand(1, "x".repeat(101), null,
                Set.of(), null, null);

        assertThatThrownBy(() -> useCase.execute(command)).isInstanceOf(ValidationException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void throwsNotFoundForUnknownPokemon() {
        when(repository.findById(42)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new UpdateLocalPokemonCommand(42, "x", null, null, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rejectsStaleVersion() {
        when(repository.findById(1)).thenReturn(Optional.of(TestData.localBulbasaur(5L)));

        assertThatThrownBy(() -> useCase.execute(new UpdateLocalPokemonCommand(1, "x", null, null, null, 4L)))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsNonPositiveId() {
        assertThatThrownBy(() -> useCase.execute(new UpdateLocalPokemonCommand(-3, null, null, null, null, null)))
                .isInstanceOf(ValidationException.class);
    }
}
