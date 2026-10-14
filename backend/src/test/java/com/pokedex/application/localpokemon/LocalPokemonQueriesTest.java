package com.pokedex.application.localpokemon;

import com.pokedex.TestData;
import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Read and delete use cases of the local Pokedex.
 */
@ExtendWith(MockitoExtension.class)
class LocalPokemonQueriesTest {

    @Mock
    private LocalPokemonRepository repository;

    @InjectMocks
    private ListLocalPokemonUseCase listLocal;
    @InjectMocks
    private GetLocalPokemonUseCase getLocal;
    @InjectMocks
    private DeleteLocalPokemonUseCase deleteLocal;

    private final PageQuery query = new PageQuery(0, 20);

    @Test
    void listPassesTrimmedSearch() {
        PageResult<LocalPokemon> page = new PageResult<>(List.of(TestData.localBulbasaur(0L)), 0, 20, 1);
        when(repository.search(query, "bulba")).thenReturn(page);

        assertThat(listLocal.execute(query, "  bulba ")).isEqualTo(page);
    }

    @Test
    void listTreatsBlankSearchAsNoFilter() {
        PageResult<LocalPokemon> empty = new PageResult<>(List.of(), 0, 20, 0);
        when(repository.search(query, null)).thenReturn(empty);

        assertThat(listLocal.execute(query, "   ")).isEqualTo(empty);
    }

    @Test
    void listRejectsOverlongSearch() {
        assertThatThrownBy(() -> listLocal.execute(query, "x".repeat(51)))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getReturnsStoredPokemon() {
        LocalPokemon bulbasaur = TestData.localBulbasaur(0L);
        when(repository.findById(1)).thenReturn(Optional.of(bulbasaur));

        assertThat(getLocal.execute(1)).isSameAs(bulbasaur);
    }

    @Test
    void getThrowsNotFound() {
        when(repository.findById(7)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getLocal.execute(7)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void deleteRemovesExistingPokemon() {
        when(repository.existsById(1)).thenReturn(true);

        deleteLocal.execute(1);

        verify(repository).deleteById(1);
    }

    @Test
    void deleteThrowsNotFoundForUnknownPokemon() {
        when(repository.existsById(1)).thenReturn(false);

        assertThatThrownBy(() -> deleteLocal.execute(1)).isInstanceOf(NotFoundException.class);
        verify(repository, never()).deleteById(1);
    }

    @Test
    void deleteRejectsNonPositiveId() {
        assertThatThrownBy(() -> deleteLocal.execute(0)).isInstanceOf(ValidationException.class);
    }
}
