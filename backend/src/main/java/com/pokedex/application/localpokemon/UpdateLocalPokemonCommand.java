package com.pokedex.application.localpokemon;

import java.util.Set;

/**
 * @param expectedVersion version the client last read; when present, a mismatch is rejected as a conflict
 */
public record UpdateLocalPokemonCommand(
        int id,
        String localizedName,
        String region,
        Set<String> tags,
        String notes,
        Long expectedVersion) {
}
