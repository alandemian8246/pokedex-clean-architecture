package com.pokedex.application.catalog;

import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonIdentifier;

/**
 * User Story 02: full detail of one Pokemon (image, stats, description and evolution chain).
 */
public class GetPokemonDetailUseCase {

    private final PokemonCatalog catalog;

    public GetPokemonDetailUseCase(PokemonCatalog catalog) {
        this.catalog = catalog;
    }

    public PokemonDetail execute(String idOrName) {
        PokemonIdentifier identifier = PokemonIdentifier.of(idOrName);
        return catalog.findDetail(identifier)
                .orElseThrow(() -> new NotFoundException("Pokemon", identifier.value()));
    }
}
