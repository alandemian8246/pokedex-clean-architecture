package com.pokedex.application.localpokemon;

import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonIdentifier;

import java.time.Clock;

/**
 * User Story 03: re-pull upstream data for an already replicated Pokemon, keeping its proprietary fields.
 */
public class RefreshLocalPokemonUseCase {

    private final PokemonCatalog catalog;
    private final LocalPokemonRepository repository;
    private final Clock clock;

    public RefreshLocalPokemonUseCase(PokemonCatalog catalog, LocalPokemonRepository repository, Clock clock) {
        this.catalog = catalog;
        this.repository = repository;
        this.clock = clock;
    }

    public LocalPokemon execute(int id) {
        LocalPokemonIds.requireValid(id);
        LocalPokemon local = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Local Pokemon", id));
        PokemonDetail detail = catalog.findDetail(PokemonIdentifier.of(String.valueOf(id)))
                .orElseThrow(() -> new NotFoundException("Pokemon", id));

        local.refreshFrom(detail, clock.instant());
        return repository.save(local);
    }
}
