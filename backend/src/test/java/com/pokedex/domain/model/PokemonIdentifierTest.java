package com.pokedex.domain.model;

import com.pokedex.domain.exception.ValidationException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PokemonIdentifierTest {

    @ParameterizedTest
    @ValueSource(strings = {"25", "pikachu", " Mr-Mime ", "deoxys-normal"})
    void acceptsIdsAndSlugs(String raw) {
        assertThat(PokemonIdentifier.of(raw).value()).isEqualTo(raw.strip().toLowerCase());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"0", "000", "-1", "pika chu", "../etc", "pikachu?x=1"})
    void rejectsInvalidValues(String raw) {
        assertThatThrownBy(() -> PokemonIdentifier.of(raw)).isInstanceOf(ValidationException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {51})
    void rejectsTooLongNames(int length) {
        String raw = "a".repeat(length);
        assertThatThrownBy(() -> PokemonIdentifier.of(raw)).isInstanceOf(ValidationException.class);
    }
}
