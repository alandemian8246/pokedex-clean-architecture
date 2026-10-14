package com.pokedex.domain.model;

import com.pokedex.TestData;
import com.pokedex.domain.exception.ConflictException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.pokedex.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalPokemonTest {

    @Test
    void fromCatalogCopiesUpstreamDataWithEmptyCustomFields() {
        LocalPokemon local = LocalPokemon.fromCatalog(TestData.bulbasaurDetail(), NOW);

        assertThat(local.id()).isEqualTo(1);
        assertThat(local.name()).isEqualTo("bulbasaur");
        assertThat(local.types()).containsExactly("grass", "poison");
        assertThat(local.stats()).hasSize(2);
        assertThat(local.customFields()).isEqualTo(CustomFields.empty());
        assertThat(local.syncedAt()).isEqualTo(NOW);
        assertThat(local.version()).isNull();
    }

    @Test
    void refreshUpdatesUpstreamDataButKeepsCustomFields() {
        LocalPokemon local = TestData.localBulbasaur(3L);
        PokemonDetail newer = new PokemonDetail(1, "bulbasaur", "Seed Pokémon", 8, 70, "s.png", "a.png",
                List.of("grass"), List.of("overgrow"), List.of(), "Updated description", null);

        local.refreshFrom(newer, NOW);

        assertThat(local.height()).isEqualTo(8);
        assertThat(local.description()).isEqualTo("Updated description");
        assertThat(local.customFields().localizedName()).isEqualTo("フシギダネ");
        assertThat(local.syncedAt()).isEqualTo(NOW);
    }

    @Test
    void refreshRejectsDataOfAnotherPokemon() {
        LocalPokemon local = TestData.localBulbasaur(0L);
        PokemonDetail other = new PokemonDetail(4, "charmander", null, 6, 85, null, null,
                List.of(), List.of(), List.of(), null, null);

        assertThatThrownBy(() -> local.refreshFrom(other, NOW)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateCustomFieldsReplacesFieldsAndTouchesUpdatedAt() {
        LocalPokemon local = TestData.localBulbasaur(0L);
        CustomFields fields = new CustomFields("Bisasam", "Kanto", Set.of("grass-starter"), "German name");

        local.updateCustomFields(fields, NOW);

        assertThat(local.customFields()).isEqualTo(fields);
        assertThat(local.updatedAt()).isEqualTo(NOW);
    }

    @Test
    void ensureVersionAcceptsMatchingOrMissingVersion() {
        LocalPokemon local = TestData.localBulbasaur(2L);

        assertThatCode(() -> local.ensureVersion(2L)).doesNotThrowAnyException();
        assertThatCode(() -> local.ensureVersion(null)).doesNotThrowAnyException();
    }

    @Test
    void ensureVersionRejectsStaleVersion() {
        LocalPokemon local = TestData.localBulbasaur(2L);

        assertThatThrownBy(() -> local.ensureVersion(1L)).isInstanceOf(ConflictException.class);
    }
}
