package com.pokedex.domain.model;

import com.pokedex.domain.exception.ValidationException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * A Pokemon reference as accepted by PokeAPI: either a positive pokedex number or a slug name.
 */
public record PokemonIdentifier(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[a-z0-9][a-z0-9-]{0,49}$");
    private static final Pattern ZERO = Pattern.compile("^0+$");

    public PokemonIdentifier {
        value = value == null ? "" : value.strip().toLowerCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches() || ZERO.matcher(value).matches()) {
            throw ValidationException.of("idOrName",
                    "must be a positive number or a name made of letters, digits and hyphens (max 50)");
        }
    }

    public static PokemonIdentifier of(String raw) {
        return new PokemonIdentifier(raw);
    }
}
