package com.pokedex.infrastructure.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Subset of the PokeAPI v2 JSON schema that this service consumes. See https://pokeapi.co/docs/v2.
 */
public final class PokeApiModels {

    private PokeApiModels() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record NamedResource(String name, String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiResource(String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Page(int count, List<NamedResource> results) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Pokemon(
            int id,
            String name,
            int height,
            int weight,
            Sprites sprites,
            List<TypeSlot> types,
            List<AbilitySlot> abilities,
            List<StatEntry> stats,
            NamedResource species) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sprites(@JsonProperty("front_default") String frontDefault, OtherSprites other) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record OtherSprites(@JsonProperty("official-artwork") Artwork officialArtwork) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Artwork(@JsonProperty("front_default") String frontDefault) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TypeSlot(int slot, NamedResource type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AbilitySlot(int slot, NamedResource ability, @JsonProperty("is_hidden") boolean hidden) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatEntry(@JsonProperty("base_stat") int baseStat, NamedResource stat) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Species(
            int id,
            String name,
            List<Genus> genera,
            @JsonProperty("flavor_text_entries") List<FlavorText> flavorTextEntries,
            @JsonProperty("evolution_chain") ApiResource evolutionChain) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Genus(String genus, NamedResource language) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FlavorText(@JsonProperty("flavor_text") String flavorText, NamedResource language) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EvolutionChain(int id, ChainLink chain) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChainLink(
            NamedResource species,
            @JsonProperty("evolution_details") List<EvolutionDetail> evolutionDetails,
            @JsonProperty("evolves_to") List<ChainLink> evolvesTo) {
    }

    /**
     * PokeAPI lists one detail per game version group; the current one is flagged with {@code is_default}.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EvolutionDetail(
            NamedResource trigger,
            @JsonProperty("min_level") Integer minLevel,
            NamedResource item,
            @JsonProperty("held_item") NamedResource heldItem,
            @JsonProperty("min_happiness") Integer minHappiness,
            @JsonProperty("known_move") NamedResource knownMove,
            @JsonProperty("known_move_type") NamedResource knownMoveType,
            NamedResource location,
            @JsonProperty("time_of_day") String timeOfDay,
            @JsonProperty("is_default") Boolean isDefault) {
    }
}
