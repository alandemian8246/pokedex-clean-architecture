package com.pokedex.application.localpokemon;

import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.model.CustomFields;
import com.pokedex.domain.model.LocalPokemon;

import java.time.Clock;

/**
 * User Story 04: edit the proprietary fields of a locally stored Pokemon.
 * Order of checks: invalid payload (400), unknown record (404), stale version (409).
 */
public class UpdateLocalPokemonUseCase {

    private final LocalPokemonRepository repository;
    private final Clock clock;

    public UpdateLocalPokemonUseCase(LocalPokemonRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public LocalPokemon execute(UpdateLocalPokemonCommand command) {
        int id = LocalPokemonIds.requireValid(command.id());
        CustomFields fields = new CustomFields(
                command.localizedName(), command.region(), command.tags(), command.notes());

        LocalPokemon local = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Local Pokemon", id));
        local.ensureVersion(command.expectedVersion());
        local.updateCustomFields(fields, clock.instant());
        return repository.save(local);
    }
}
