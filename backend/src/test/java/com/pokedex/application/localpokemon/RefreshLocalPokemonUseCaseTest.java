package com.pokedex.application.localpokemon;

import com.pokedex.TestData;
import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PokemonIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class RefreshLocalPokemonUseCaseTest {

    @Mock
    private PokemonCatalog catalog;
    @Mock
    private LocalPokemonRepository repository;

    private RefreshLocalPokemonUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RefreshLocalPokemonUseCase(catalog, repository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void refreshesUpstreamDataAndKeepsCustomFields() {
        when(repository.findById(1)).thenReturn(Optional.of(TestData.localBulbasaur(4L)));
        when(catalog.findDetail(new PokemonIdentifier("1"))).thenReturn(Optional.of(TestData.bulbasaurDetail()));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LocalPokemon refreshed = useCase.execute(1);

        assertThat(refreshed.syncedAt()).isEqualTo(NOW);
        assertThat(refreshed.stats()).hasSize(2);
        assertThat(refreshed.customFields().localizedName()).isEqualTo("フシギダネ");
        assertThat(refreshed.version()).isEqualTo(4L);
    }

    @Test
    void throwsNotFoundWhenNotStoredLocally() {
        when(repository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(99)).isInstanceOf(NotFoundException.class);
        verifyNoInteractions(catalog);
    }

    @Test
    void throwsNotFoundWhenRemovedUpstream() {
        when(repository.findById(1)).thenReturn(Optional.of(TestData.localBulbasaur(0L)));
        when(catalog.findDetail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(1)).isInstanceOf(NotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void rejectsNonPositiveId() {
        assertThatThrownBy(() -> useCase.execute(0)).isInstanceOf(ValidationException.class);
    }
}
