package com.pokedex.web.localpokemon;

import com.pokedex.domain.model.CustomFields;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.web.pokemon.PokemonDetailResponse.StatResponse;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public record LocalPokemonResponse(
        int id,
        String name,
        String category,
        double heightM,
        double weightKg,
        String spriteUrl,
        String artworkUrl,
        String description,
        List<String> types,
        List<String> abilities,
        List<StatResponse> stats,
        String localizedName,
        String region,
        Set<String> tags,
        String notes,
        Instant syncedAt,
        Instant updatedAt,
        Long version) {

    static LocalPokemonResponse from(LocalPokemon pokemon) {
        CustomFields custom = pokemon.customFields();
        return new LocalPokemonResponse(
                pokemon.id(),
                pokemon.name(),
                pokemon.category(),
                pokemon.height() / 10.0,
                pokemon.weight() / 10.0,
                pokemon.spriteUrl(),
                pokemon.artworkUrl(),
                pokemon.description(),
                pokemon.types(),
                pokemon.abilities(),
                pokemon.stats().stream().map(stat -> new StatResponse(stat.name(), stat.value())).toList(),
                custom.localizedName(),
                custom.region(),
                custom.tags(),
                custom.notes(),
                pokemon.syncedAt(),
                pokemon.updatedAt(),
                pokemon.version());
    }
}
