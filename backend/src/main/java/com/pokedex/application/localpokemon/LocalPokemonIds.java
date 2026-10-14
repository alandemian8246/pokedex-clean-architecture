package com.pokedex.application.localpokemon;

import com.pokedex.domain.exception.ValidationException;

final class LocalPokemonIds {

    private LocalPokemonIds() {
    }

    static int requireValid(int id) {
        if (id <= 0) {
            throw ValidationException.of("id", "must be a positive number");
        }
        return id;
    }
}
