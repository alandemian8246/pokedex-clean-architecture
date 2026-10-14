package com.pokedex;

import com.pokedex.domain.model.CustomFields;
import com.pokedex.domain.model.EvolutionNode;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonSummary;
import com.pokedex.domain.model.Stat;

import java.time.Instant;
import java.util.List;
import java.util.Set;

/**
 * Shared test fixtures.
 */
public final class TestData {

    public static final Instant NOW = Instant.parse("2026-10-14T10:00:00Z");

    private TestData() {
    }

    public static PokemonSummary bulbasaurSummary() {
        return new PokemonSummary(1, "bulbasaur", "Seed Pokémon", 69, "sprite-1.png",
                List.of("grass", "poison"), List.of("overgrow", "chlorophyll"));
    }

    public static PokemonDetail bulbasaurDetail() {
        EvolutionNode venusaur = new EvolutionNode(3, "venusaur", "3.png", "Level 32", List.of());
        EvolutionNode ivysaur = new EvolutionNode(2, "ivysaur", "2.png", "Level 16", List.of(venusaur));
        EvolutionNode chain = new EvolutionNode(1, "bulbasaur", "1.png", null, List.of(ivysaur));
        return new PokemonDetail(1, "bulbasaur", "Seed Pokémon", 7, 69, "sprite-1.png", "artwork-1.png",
                List.of("grass", "poison"), List.of("overgrow", "chlorophyll"),
                List.of(new Stat("hp", 45), new Stat("attack", 49)),
                "A strange seed was planted on its back at birth.", chain);
    }

    public static LocalPokemon localBulbasaur(Long version) {
        return new LocalPokemon(1, "bulbasaur", "Seed Pokémon", 7, 69, "sprite-1.png", "artwork-1.png",
                "A strange seed was planted on its back at birth.", List.of("grass", "poison"),
                List.of("overgrow", "chlorophyll"), List.of(new Stat("hp", 45)),
                new CustomFields("フシギダネ", "Kanto", Set.of("starter"), null),
                NOW.minusSeconds(3600), NOW.minusSeconds(3600), version);
    }
}
