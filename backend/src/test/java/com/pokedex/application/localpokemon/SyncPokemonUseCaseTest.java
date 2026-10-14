package com.pokedex.application.localpokemon;

import com.pokedex.TestData;
import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.domain.exception.ConflictException;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PokemonIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Optional;

import static com.pokedex.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncPokemonUseCaseTest {

    @Mock
    private PokemonCatalog catalog;
    @Mock
    private LocalPokemonRepository repository;

    private SyncPokemonUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SyncPokemonUseCase(catalog, repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void storesTheCatalogPokemonLocally() {
        when(catalog.findDetail(new PokemonIdentifier("bulbasaur"))).thenReturn(Optional.of(TestData.bulbasaurDetail()));
        when(repository.existsById(1)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalPokemon saved = useCase.execute("bulbasaur");

        ArgumentCaptor<LocalPokemon> captor = ArgumentCaptor.forClass(LocalPokemon.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().id()).isEqualTo(1);
        assertThat(captor.getValue().syncedAt()).isEqualTo(NOW);
        assertThat(saved.name()).isEqualTo("bulbasaur");
    }

    @Test
    void rejectsPokemonAlreadyStoredLocally() {
        when(catalog.findDetail(any())).thenReturn(Optional.of(TestData.bulbasaurDetail()));
        when(repository.existsById(1)).thenReturn(true);

        assertThatThrownBy(() -> useCase.execute("1")).isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void throwsNotFoundWhenPokemonDoesNotExistUpstream() {
        when(catalog.findDetail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("missingno")).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsMalformedIdentifier() {
        assertThatThrownBy(() -> useCase.execute(" ")).isInstanceOf(ValidationException.class);
        verifyNoInteractions(catalog, repository);
    }
}
