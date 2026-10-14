package com.pokedex.application.localpokemon;

import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.domain.exception.ConflictException;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonIdentifier;

import java.time.Clock;

/**
 * User Story 03: replicate a Pokemon from PokeAPI into the local store so proprietary fields can be attached.
 */
public class SyncPokemonUseCase {

    private final PokemonCatalog catalog;
    private final LocalPokemonRepository repository;
    private final Clock clock;

    public SyncPokemonUseCase(PokemonCatalog catalog, LocalPokemonRepository repository, Clock clock) {
        this.catalog = catalog;
        this.repository = repository;
        this.clock = clock;
    }

    public LocalPokemon execute(String idOrName) {
        PokemonIdentifier identifier = PokemonIdentifier.of(idOrName);
        PokemonDetail detail = catalog.findDetail(identifier)
                .orElseThrow(() -> new NotFoundException("Pokemon", identifier.value()));

        if (repository.existsById(detail.id())) {
            throw new ConflictException(
                    "Pokemon %d (%s) is already in the local Pokedex".formatted(detail.id(), detail.name()));
        }
        return repository.save(LocalPokemon.fromCatalog(detail, clock.instant()));
    }
}
