package com.pokedex.domain.model;

import java.util.List;

/**
 * Full Pokemon view (User Story 02). Height is in decimetres and weight in hectograms, as provided by PokeAPI.
 */
public record PokemonDetail(
        int id,
        String name,
        String category,
        int height,
        int weight,
        String spriteUrl,
        String artworkUrl,
        List<String> types,
        List<String> abilities,
        List<Stat> stats,
        String description,
        EvolutionNode evolutionChain) {

    public PokemonDetail {
        types = types == null ? List.of() : List.copyOf(types);
        abilities = abilities == null ? List.of() : List.copyOf(abilities);
        stats = stats == null ? List.of() : List.copyOf(stats);
    }
}
