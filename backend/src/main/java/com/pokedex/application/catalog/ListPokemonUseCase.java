package com.pokedex.application.catalog;

import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import com.pokedex.domain.model.PokemonSummary;

/**
 * User Story 01: browse Pokemon page by page.
 */
public class ListPokemonUseCase {

    private final PokemonCatalog catalog;

    public ListPokemonUseCase(PokemonCatalog catalog) {
        this.catalog = catalog;
    }

    public PageResult<PokemonSummary> execute(PageQuery query) {
        return catalog.list(query);
    }
}
