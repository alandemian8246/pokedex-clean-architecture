package com.pokedex.infrastructure.persistence;

import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.domain.model.CustomFields;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import com.pokedex.domain.model.Stat;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;

/**
 * Adapter implementing the {@link LocalPokemonRepository} port with Spring Data JPA.
 */
@Repository
@Transactional(readOnly = true)
public class JpaLocalPokemonRepository implements LocalPokemonRepository {

    private final SpringDataLocalPokemonRepository jpa;

    public JpaLocalPokemonRepository(SpringDataLocalPokemonRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<LocalPokemon> findById(int id) {
        return jpa.findById(id).map(JpaLocalPokemonRepository::toDomain);
    }

    @Override
    public boolean existsById(int id) {
        return jpa.existsById(id);
    }

    @Override
    @Transactional
    public LocalPokemon save(LocalPokemon pokemon) {
        // saveAndFlush surfaces optimistic-locking and constraint violations inside this call
        return toDomain(jpa.saveAndFlush(toEntity(pokemon)));
    }

    @Override
    @Transactional
    public void deleteById(int id) {
        jpa.deleteById(id);
    }

    @Override
    public PageResult<LocalPokemon> search(PageQuery query, String search) {
        PageRequest pageable = PageRequest.of(query.page(), query.size(), Sort.by("id"));
        Page<LocalPokemonEntity> page = search == null
                ? jpa.findAll(pageable)
                : jpa.search(likePattern(search), pageable);
        return new PageResult<>(page.map(JpaLocalPokemonRepository::toDomain).getContent(),
                query.page(), query.size(), page.getTotalElements());
    }

    private static String likePattern(String search) {
        String escaped = search.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escaped + "%";
    }

    static LocalPokemon toDomain(LocalPokemonEntity entity) {
        return new LocalPokemon(
                entity.getId(),
                entity.getName(),
                entity.getCategory(),
                entity.getHeight(),
                entity.getWeight(),
                entity.getSpriteUrl(),
                entity.getArtworkUrl(),
                entity.getDescription(),
                entity.getTypes(),
                entity.getAbilities(),
                entity.getStats().stream().map(stat -> new Stat(stat.getName(), stat.getBaseValue())).toList(),
                new CustomFields(entity.getLocalizedName(), entity.getRegion(),
                        new LinkedHashSet<>(entity.getTags()), entity.getNotes()),
                entity.getSyncedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }

    static LocalPokemonEntity toEntity(LocalPokemon pokemon) {
        CustomFields custom = pokemon.customFields();
        return new LocalPokemonEntity(
                pokemon.id(),
                pokemon.name(),
                pokemon.category(),
                pokemon.height(),
                pokemon.weight(),
                pokemon.spriteUrl(),
                pokemon.artworkUrl(),
                pokemon.description(),
                pokemon.types(),
                pokemon.abilities(),
                pokemon.stats().stream().map(stat -> new StatEmbeddable(stat.name(), stat.value())).toList(),
                custom.localizedName(),
                custom.region(),
                new ArrayList<>(custom.tags()),
                custom.notes(),
                pokemon.syncedAt(),
                pokemon.updatedAt(),
                pokemon.version());
    }
}
