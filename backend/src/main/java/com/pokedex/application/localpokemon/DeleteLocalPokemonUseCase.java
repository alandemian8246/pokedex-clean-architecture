package com.pokedex.application.localpokemon;

import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.domain.exception.NotFoundException;

public class DeleteLocalPokemonUseCase {

    private final LocalPokemonRepository repository;

    public DeleteLocalPokemonUseCase(LocalPokemonRepository repository) {
        this.repository = repository;
    }

    public void execute(int id) {
        LocalPokemonIds.requireValid(id);
        if (!repository.existsById(id)) {
            throw new NotFoundException("Local Pokemon", id);
        }
        repository.deleteById(id);
    }
}
