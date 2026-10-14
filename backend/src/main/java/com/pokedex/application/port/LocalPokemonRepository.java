package com.pokedex.application.port;

import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;

import java.util.Optional;

/**
 * Output port to the local relational replica of Pokemon data.
 */
public interface LocalPokemonRepository {

    Optional<LocalPokemon> findById(int id);

    boolean existsById(int id);

    LocalPokemon save(LocalPokemon pokemon);

    void deleteById(int id);

    /**
     * @param search optional case-insensitive text matched against the name and the localized name
     */
    PageResult<LocalPokemon> search(PageQuery query, String search);
}
