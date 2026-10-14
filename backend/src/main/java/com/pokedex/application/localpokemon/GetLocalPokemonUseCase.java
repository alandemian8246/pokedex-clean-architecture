package com.pokedex.application.localpokemon;

import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.model.LocalPokemon;

public class GetLocalPokemonUseCase {

    private final LocalPokemonRepository repository;

    public GetLocalPokemonUseCase(LocalPokemonRepository repository) {
        this.repository = repository;
    }

    public LocalPokemon execute(int id) {
        LocalPokemonIds.requireValid(id);
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Local Pokemon", id));
    }
}
