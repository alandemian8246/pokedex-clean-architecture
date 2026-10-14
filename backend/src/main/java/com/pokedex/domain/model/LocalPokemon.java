package com.pokedex.domain.model;

import com.pokedex.domain.exception.ConflictException;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root for a Pokemon replicated into the local store. Core attributes mirror PokeAPI and are only
 * changed by a refresh; {@link CustomFields} are owned by this system and are editable (User Story 04).
 */
public final class LocalPokemon {

    private final int id;
    private String name;
    private String category;
    private int height;
    private int weight;
    private String spriteUrl;
    private String artworkUrl;
    private String description;
    private List<String> types;
    private List<String> abilities;
    private List<Stat> stats;
    private CustomFields customFields;
    private Instant syncedAt;
    private Instant updatedAt;
    private final Long version;

    @SuppressWarnings("java:S107") // rehydration constructor mirrors the persisted state
    public LocalPokemon(int id, String name, String category, int height, int weight, String spriteUrl,
                        String artworkUrl, String description, List<String> types, List<String> abilities,
                        List<Stat> stats, CustomFields customFields, Instant syncedAt, Instant updatedAt,
                        Long version) {
        this.id = id;
        this.name = Objects.requireNonNull(name, "name");
        this.category = category;
        this.height = height;
        this.weight = weight;
        this.spriteUrl = spriteUrl;
        this.artworkUrl = artworkUrl;
        this.description = description;
        this.types = List.copyOf(types);
        this.abilities = List.copyOf(abilities);
        this.stats = List.copyOf(stats);
        this.customFields = Objects.requireNonNull(customFields, "customFields");
        this.syncedAt = Objects.requireNonNull(syncedAt, "syncedAt");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt");
        this.version = version;
    }

    public static LocalPokemon fromCatalog(PokemonDetail detail, Instant now) {
        return new LocalPokemon(detail.id(), detail.name(), detail.category(), detail.height(), detail.weight(),
                detail.spriteUrl(), detail.artworkUrl(), detail.description(), detail.types(), detail.abilities(),
                detail.stats(), CustomFields.empty(), now, now, null);
    }

    /** Re-applies upstream data while preserving every proprietary field. */
    public void refreshFrom(PokemonDetail detail, Instant now) {
        if (detail.id() != id) {
            throw new IllegalArgumentException("Cannot refresh Pokemon %d with data of %d".formatted(id, detail.id()));
        }
        this.name = detail.name();
        this.category = detail.category();
        this.height = detail.height();
        this.weight = detail.weight();
        this.spriteUrl = detail.spriteUrl();
        this.artworkUrl = detail.artworkUrl();
        this.description = detail.description();
        this.types = detail.types();
        this.abilities = detail.abilities();
        this.stats = detail.stats();
        this.syncedAt = now;
        this.updatedAt = now;
    }

    public void updateCustomFields(CustomFields fields, Instant now) {
        this.customFields = Objects.requireNonNull(fields, "fields");
        this.updatedAt = now;
    }

    /** Optimistic concurrency guard: rejects an update based on a stale copy of the record. */
    public void ensureVersion(Long expectedVersion) {
        if (expectedVersion != null && !expectedVersion.equals(version)) {
            throw new ConflictException(
                    "Pokemon %d was modified by someone else (expected version %d, current %d)"
                            .formatted(id, expectedVersion, version));
        }
    }

    public int id() { return id; }
    public String name() { return name; }
    public String category() { return category; }
    public int height() { return height; }
    public int weight() { return weight; }
    public String spriteUrl() { return spriteUrl; }
    public String artworkUrl() { return artworkUrl; }
    public String description() { return description; }
    public List<String> types() { return types; }
    public List<String> abilities() { return abilities; }
    public List<Stat> stats() { return stats; }
    public CustomFields customFields() { return customFields; }
    public Instant syncedAt() { return syncedAt; }
    public Instant updatedAt() { return updatedAt; }
    public Long version() { return version; }
}
