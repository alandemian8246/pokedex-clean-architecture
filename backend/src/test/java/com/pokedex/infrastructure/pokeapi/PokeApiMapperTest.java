package com.pokedex.infrastructure.pokeapi;

import com.pokedex.domain.model.EvolutionNode;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.ChainLink;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.EvolutionChain;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.EvolutionDetail;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.NamedResource;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.Pokemon;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PokeApiMapperTest {

    @Test
    void extractsIdFromResourceUrls() {
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/pokemon-species/133/")).contains(133);
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/evolution-chain/67")).contains(67);
        assertThat(PokeApiMapper.idFromUrl("https://pokeapi.co/api/v2/pokemon/")).isEmpty();
        assertThat(PokeApiMapper.idFromUrl(null)).isEmpty();
    }

    @Test
    void describesEvolutionTriggers() {
        assertThat(PokeApiMapper.describeTrigger(List.of(detail("use-item", null, named("thunder-stone"), null, null))))
                .isEqualTo("Use thunder stone");
        assertThat(PokeApiMapper.describeTrigger(List.of(detail("trade", null, null, named("metal-coat"), null))))
                .isEqualTo("Trade holding metal coat");
        assertThat(PokeApiMapper.describeTrigger(List.of(detail("trade", null, null, null, null))))
                .isEqualTo("Trade");
        assertThat(PokeApiMapper.describeTrigger(List.of(detail("level-up", null, null, null, 160))))
                .isEqualTo("Level up with high friendship");
        assertThat(PokeApiMapper.describeTrigger(List.of(new EvolutionDetail(named("level-up"), null, null, null,
                160, null, null, null, "day", null)))).isEqualTo("Level up with high friendship (day)");
        assertThat(PokeApiMapper.describeTrigger(List.of(new EvolutionDetail(named("level-up"), null, null, null,
                160, null, named("fairy"), null, "", true)))).isEqualTo("Level up knowing a fairy-type move");
        assertThat(PokeApiMapper.describeTrigger(List.of(new EvolutionDetail(named("level-up"), null, null, null,
                null, null, null, named("eterna-forest"), "", null)))).isEqualTo("Level up at eterna forest");
        assertThat(PokeApiMapper.describeTrigger(List.of(detail("shed", null, null, null, null))))
                .isEqualTo("Shed");
        assertThat(PokeApiMapper.describeTrigger(List.of())).isNull();
    }

    @Test
    void prefersTheDefaultEvolutionDetailAcrossGameVersions() {
        EvolutionDetail oldGames = new EvolutionDetail(named("level-up"), null, null, null, null, null, null,
                named("eterna-forest"), "", null);
        EvolutionDetail current = new EvolutionDetail(named("use-item"), null, named("leaf-stone"), null, null,
                null, null, null, "", true);

        assertThat(PokeApiMapper.describeTrigger(List.of(oldGames, current))).isEqualTo("Use leaf stone");
    }

    @Test
    void mapsBranchingEvolutionChains() {
        ChainLink vaporeon = link(134, "vaporeon", List.of(detail("use-item", null, named("water-stone"), null, null)));
        ChainLink jolteon = link(135, "jolteon", List.of(detail("use-item", null, named("thunder-stone"), null, null)));
        ChainLink eevee = new ChainLink(species(133, "eevee"), List.of(), List.of(vaporeon, jolteon));
        Pokemon pokemon = new Pokemon(133, "eevee", 3, 65, null, null, null, null, null);

        PokemonDetail detail = PokeApiMapper.toDetail(pokemon, null, new EvolutionChain(67, eevee));

        EvolutionNode root = detail.evolutionChain();
        assertThat(root.evolvesTo()).extracting(EvolutionNode::name).containsExactly("vaporeon", "jolteon");
        assertThat(root.evolvesTo().get(1).spriteUrl()).endsWith("/135.png");
        assertThat(detail.category()).isNull();
        assertThat(detail.description()).isNull();
        assertThat(detail.spriteUrl()).isNull();
        assertThat(detail.types()).isEmpty();
    }

    private static ChainLink link(int id, String name, List<EvolutionDetail> details) {
        return new ChainLink(species(id, name), details, List.of());
    }

    private static NamedResource species(int id, String name) {
        return new NamedResource(name, "https://pokeapi.co/api/v2/pokemon-species/" + id + "/");
    }

    private static NamedResource named(String name) {
        return new NamedResource(name, null);
    }

    private static EvolutionDetail detail(String trigger, Integer minLevel, NamedResource item,
                                          NamedResource heldItem, Integer minHappiness) {
        return new EvolutionDetail(named(trigger), minLevel, item, heldItem, minHappiness, null, null, null, "", null);
    }
}
