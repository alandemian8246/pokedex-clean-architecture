package com.pokedex.application.localpokemon;

import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;

public class ListLocalPokemonUseCase {

    static final int MAX_SEARCH_LENGTH = 50;

    private final LocalPokemonRepository repository;

    public ListLocalPokemonUseCase(LocalPokemonRepository repository) {
        this.repository = repository;
    }

    public PageResult<LocalPokemon> execute(PageQuery query, String search) {
        String normalized = search == null || search.isBlank() ? null : search.strip();
        if (normalized != null && normalized.length() > MAX_SEARCH_LENGTH) {
            throw ValidationException.of("search", "must be at most " + MAX_SEARCH_LENGTH + " characters");
        }
        return repository.search(query, normalized);
    }
}
