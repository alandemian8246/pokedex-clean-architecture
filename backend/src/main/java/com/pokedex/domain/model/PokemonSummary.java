package com.pokedex.domain.model;

import java.util.List;

/**
 * Lightweight projection used by the paginated catalog (User Story 01).
 * Weight is expressed in hectograms, as provided by PokeAPI.
 */
public record PokemonSummary(
        int id,
        String name,
        String category,
        int weight,
        String spriteUrl,
        List<String> types,
        List<String> abilities) {

    public PokemonSummary {
        types = types == null ? List.of() : List.copyOf(types);
        abilities = abilities == null ? List.of() : List.copyOf(abilities);
    }
}
