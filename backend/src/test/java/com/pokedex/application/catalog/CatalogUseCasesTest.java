package com.pokedex.application.catalog;

import com.pokedex.TestData;
import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonIdentifier;
import com.pokedex.domain.model.PokemonSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogUseCasesTest {

    @Mock
    private PokemonCatalog catalog;

    @InjectMocks
    private ListPokemonUseCase listPokemon;

    @InjectMocks
    private GetPokemonDetailUseCase getPokemonDetail;

    @Test
    void listDelegatesToCatalogWithTheRequestedPage() {
        PageQuery query = new PageQuery(1, 10);
        PageResult<PokemonSummary> page = new PageResult<>(List.of(TestData.bulbasaurSummary()), 1, 10, 1302);
        when(catalog.list(query)).thenReturn(page);

        assertThat(listPokemon.execute(query)).isEqualTo(page);
    }

    @Test
    void detailNormalizesTheIdentifier() {
        PokemonDetail detail = TestData.bulbasaurDetail();
        when(catalog.findDetail(new PokemonIdentifier("bulbasaur"))).thenReturn(Optional.of(detail));

        assertThat(getPokemonDetail.execute("  BULBASAUR ")).isEqualTo(detail);
    }

    @Test
    void detailThrowsNotFoundWhenCatalogHasNoMatch() {
        when(catalog.findDetail(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getPokemonDetail.execute("missingno"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("missingno");
        verify(catalog).findDetail(new PokemonIdentifier("missingno"));
    }

    @Test
    void detailRejectsMalformedIdentifierWithoutCallingCatalog() {
        assertThatThrownBy(() -> getPokemonDetail.execute("../../admin"))
                .isInstanceOf(ValidationException.class);
        verifyNoInteractions(catalog);
    }
}
