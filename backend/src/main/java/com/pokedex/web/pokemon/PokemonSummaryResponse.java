package com.pokedex.web.pokemon;

import com.pokedex.domain.model.PokemonSummary;

import java.util.List;

/**
 * @param weight weight in hectograms (PokeAPI unit); {@code weightKg} is provided for display convenience
 */
public record PokemonSummaryResponse(
        int id,
        String name,
        String category,
        int weight,
        double weightKg,
        String spriteUrl,
        List<String> types,
        List<String> abilities) {

    static PokemonSummaryResponse from(PokemonSummary summary) {
        return new PokemonSummaryResponse(summary.id(), summary.name(), summary.category(), summary.weight(),
                summary.weight() / 10.0, summary.spriteUrl(), summary.types(), summary.abilities());
    }
}
