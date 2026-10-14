package com.pokedex.infrastructure.persistence;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA representation of {@link com.pokedex.domain.model.LocalPokemon}. Kept separate from the domain model so
 * persistence concerns (annotations, mutable collections, proxies) never leak into business logic.
 */
@Entity
@Table(name = "local_pokemon")
public class LocalPokemonEntity {

    @Id
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 100)
    private String category;

    private int height;

    private int weight;

    @Column(name = "sprite_url", length = 500)
    private String spriteUrl;

    @Column(name = "artwork_url", length = 500)
    private String artworkUrl;

    @Column(length = 1000)
    private String description;

    @ElementCollection
    @CollectionTable(name = "local_pokemon_type", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    @Column(name = "type_name", nullable = false, length = 30)
    private List<String> types = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "local_pokemon_ability", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    @Column(name = "ability_name", nullable = false, length = 50)
    private List<String> abilities = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "local_pokemon_stat", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    private List<StatEmbeddable> stats = new ArrayList<>();

    @Column(name = "localized_name", length = 100)
    private String localizedName;

    @Column(length = 50)
    private String region;

    @ElementCollection
    @CollectionTable(name = "local_pokemon_tag", joinColumns = @JoinColumn(name = "pokemon_id"))
    @OrderColumn(name = "position")
    @Column(name = "tag", nullable = false, length = 30)
    private List<String> tags = new ArrayList<>();

    @Column(length = 500)
    private String notes;

    @Column(name = "synced_at", nullable = false)
    private Instant syncedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    protected LocalPokemonEntity() {
        // required by JPA
    }

    @SuppressWarnings("java:S107")
    LocalPokemonEntity(Integer id, String name, String category, int height, int weight, String spriteUrl,
                       String artworkUrl, String description, List<String> types, List<String> abilities,
                       List<StatEmbeddable> stats, String localizedName, String region, List<String> tags,
                       String notes, Instant syncedAt, Instant updatedAt, Long version) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.height = height;
        this.weight = weight;
        this.spriteUrl = spriteUrl;
        this.artworkUrl = artworkUrl;
        this.description = description;
        this.types = new ArrayList<>(types);
        this.abilities = new ArrayList<>(abilities);
        this.stats = new ArrayList<>(stats);
        this.localizedName = localizedName;
        this.region = region;
        this.tags = new ArrayList<>(tags);
        this.notes = notes;
        this.syncedAt = syncedAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getHeight() { return height; }
    public int getWeight() { return weight; }
    public String getSpriteUrl() { return spriteUrl; }
    public String getArtworkUrl() { return artworkUrl; }
    public String getDescription() { return description; }
    public List<String> getTypes() { return types; }
    public List<String> getAbilities() { return abilities; }
    public List<StatEmbeddable> getStats() { return stats; }
    public String getLocalizedName() { return localizedName; }
    public String getRegion() { return region; }
    public List<String> getTags() { return tags; }
    public String getNotes() { return notes; }
    public Instant getSyncedAt() { return syncedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
}
