package com.pokedex.application.port;

import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonIdentifier;
import com.pokedex.domain.model.PokemonSummary;

import java.util.Optional;

/**
 * Output port to the external source of truth for Pokemon data (implemented by the PokeAPI adapter).
 */
public interface PokemonCatalog {

    PageResult<PokemonSummary> list(PageQuery query);

    Optional<PokemonDetail> findDetail(PokemonIdentifier identifier);
}
