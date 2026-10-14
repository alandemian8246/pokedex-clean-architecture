package com.pokedex.infrastructure.persistence;

import com.pokedex.TestData;
import com.pokedex.domain.model.CustomFields;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import com.pokedex.domain.model.Role;
import com.pokedex.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static com.pokedex.TestData.NOW;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the real Flyway migrations (including seed data) against H2 in PostgreSQL mode.
 * Tests run without a surrounding transaction so optimistic locking behaves as in production.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaLocalPokemonRepository.class, JpaUserRepository.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class JpaRepositoriesTest {

    @Autowired
    private JpaLocalPokemonRepository pokemonRepository;
    @Autowired
    private JpaUserRepository userRepository;

    @Test
    void seedDataIsAvailableWithAllCollections() {
        LocalPokemon pikachu = pokemonRepository.findById(25).orElseThrow();

        assertThat(pikachu.name()).isEqualTo("pikachu");
        assertThat(pikachu.types()).containsExactly("electric");
        assertThat(pikachu.stats()).hasSize(6);
        assertThat(pikachu.customFields().localizedName()).isEqualTo("ピカチュウ");
        assertThat(pikachu.customFields().tags()).containsExactly("mascot", "gen-1", "fan-favorite");
    }

    @Test
    void searchMatchesNameOrLocalizedNameCaseInsensitively() {
        PageResult<LocalPokemon> byName = pokemonRepository.search(new PageQuery(0, 10), "CHAR");
        PageResult<LocalPokemon> byLocalizedName = pokemonRepository.search(new PageQuery(0, 10), "イーブイ");
        PageResult<LocalPokemon> wildcardIsLiteral = pokemonRepository.search(new PageQuery(0, 10), "%");

        assertThat(byName.items()).extracting(LocalPokemon::name).containsExactly("charmander");
        assertThat(byLocalizedName.items()).extracting(LocalPokemon::name).containsExactly("eevee");
        assertThat(wildcardIsLiteral.items()).isEmpty();
    }

    @Test
    void listIsPaginatedAndOrderedById() {
        PageResult<LocalPokemon> page = pokemonRepository.search(new PageQuery(0, 2), null);

        assertThat(page.items()).extracting(LocalPokemon::id).containsExactly(1, 4);
        assertThat(page.totalElements()).isGreaterThanOrEqualTo(5);
    }

    @Test
    void createUpdateAndDeleteRoundTrip() {
        LocalPokemon created = pokemonRepository.save(LocalPokemon.fromCatalog(withId(150, "mewtwo"), NOW));
        assertThat(created.version()).isZero();

        created.updateCustomFields(new CustomFields("ミュウツー", "Cerulean Cave", Set.of("legendary"), null), NOW);
        LocalPokemon updated = pokemonRepository.save(created);
        assertThat(updated.version()).isEqualTo(1L);
        assertThat(pokemonRepository.findById(150).orElseThrow().customFields().tags()).containsExactly("legendary");

        pokemonRepository.deleteById(150);
        assertThat(pokemonRepository.existsById(150)).isFalse();
    }

    @Test
    void savingAStaleCopyFailsWithOptimisticLock() {
        pokemonRepository.save(LocalPokemon.fromCatalog(withId(151, "mew"), NOW));
        LocalPokemon first = pokemonRepository.findById(151).orElseThrow();
        LocalPokemon second = pokemonRepository.findById(151).orElseThrow();

        first.updateCustomFields(new CustomFields("A", null, Set.of(), null), NOW);
        pokemonRepository.save(first);
        second.updateCustomFields(new CustomFields("B", null, Set.of(), null), NOW);

        assertThatThrownBy(() -> pokemonRepository.save(second))
                .isInstanceOf(OptimisticLockingFailureException.class);
        pokemonRepository.deleteById(151);
    }

    @Test
    void usersAreStoredWithGeneratedIdAndUniqueUsername() {
        User saved = userRepository.save(User.newUser("misty", "hash", Role.USER, NOW));

        assertThat(saved.id()).isNotNull();
        assertThat(userRepository.existsByUsername("misty")).isTrue();
        assertThat(userRepository.findByUsername("misty")).get().extracting(User::role).isEqualTo(Role.USER);
        assertThatThrownBy(() -> userRepository.save(User.newUser("misty", "other", Role.USER, NOW)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static com.pokedex.domain.model.PokemonDetail withId(int id, String name) {
        var base = TestData.bulbasaurDetail();
        return new com.pokedex.domain.model.PokemonDetail(id, name, "Genetic Pokémon", base.height(), base.weight(),
                base.spriteUrl(), base.artworkUrl(), base.types(), base.abilities(), base.stats(),
                base.description(), null);
    }
}
