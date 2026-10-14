package com.pokedex.web.localpokemon;

import java.util.Set;

/**
 * Full replacement of the editable (proprietary) fields. Business rules are enforced by the domain;
 * {@code version}, when sent, enables optimistic concurrency control.
 */
public record UpdateLocalPokemonRequest(
        String localizedName,
        String region,
        Set<String> tags,
        String notes,
        Long version) {
}
