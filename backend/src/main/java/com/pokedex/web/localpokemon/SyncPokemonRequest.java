package com.pokedex.web.localpokemon;

import jakarta.validation.constraints.NotBlank;

public record SyncPokemonRequest(@NotBlank String idOrName) {
}
