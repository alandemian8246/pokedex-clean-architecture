package com.pokedex.infrastructure.pokeapi;

import com.pokedex.domain.exception.ExternalServiceException;
import com.pokedex.domain.model.EvolutionNode;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonIdentifier;
import com.pokedex.domain.model.PokemonSummary;
import com.pokedex.domain.model.Stat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.http.HttpMethod.GET;

/**
 * Exercises the PokeAPI adapter against recorded PokeAPI payloads, without network access.
 */
class PokeApiCatalogAdapterTest {

    private static final String BASE = "https://pokeapi.test/api/v2";

    private MockRestServiceServer server;
    private PokeApiCatalogAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        server = MockRestServiceServer.bindTo(builder).build();
        // synchronous executor keeps request order deterministic for MockRestServiceServer
        adapter = new PokeApiCatalogAdapter(new PokeApiClient(builder.build()), Runnable::run);
    }

    @Test
    void listEnrichesEachEntryWithPokemonAndSpeciesData() throws IOException {
        expect("/pokemon?offset=0&limit=1", "page-0-1.json");
        expect("/pokemon/bulbasaur", "pokemon-1.json");
        expect("/pokemon-species/1", "species-1.json");

        PageResult<PokemonSummary> page = adapter.list(new PageQuery(0, 1));

        assertThat(page.totalElements()).isEqualTo(1302);
        assertThat(page.items()).singleElement().satisfies(summary -> {
            assertThat(summary.id()).isEqualTo(1);
            assertThat(summary.category()).isEqualTo("Seed Pokémon");
            assertThat(summary.weight()).isEqualTo(69);
            assertThat(summary.spriteUrl()).endsWith("/pokemon/1.png");
            assertThat(summary.types()).containsExactly("grass", "poison");
            assertThat(summary.abilities()).containsExactly("overgrow", "chlorophyll");
        });
        server.verify();
    }

    @Test
    void detailCombinesPokemonSpeciesAndEvolutionChain() throws IOException {
        expect("/pokemon/1", "pokemon-1.json");
        expect("/pokemon-species/1", "species-1.json");
        expect("/evolution-chain/1", "evolution-chain-1.json");

        PokemonDetail detail = adapter.findDetail(PokemonIdentifier.of("1")).orElseThrow();

        assertThat(detail.artworkUrl()).contains("official-artwork");
        assertThat(detail.stats()).hasSize(6).first().isEqualTo(new Stat("hp", 45));
        assertThat(detail.description())
                .isEqualTo("A strange seed was planted on its back at birth. The plant sprouts and grows with this POKéMON.");
        EvolutionNode chain = detail.evolutionChain();
        assertThat(chain.name()).isEqualTo("bulbasaur");
        assertThat(chain.trigger()).isNull();
        EvolutionNode ivysaur = chain.evolvesTo().getFirst();
        assertThat(ivysaur.id()).isEqualTo(2);
        assertThat(ivysaur.trigger()).isEqualTo("Level 16");
        assertThat(ivysaur.evolvesTo().getFirst().trigger()).isEqualTo("Level 32");
        server.verify();
    }

    @Test
    void detailIsEmptyWhenPokeApiReturns404() {
        server.expect(once(), requestTo(BASE + "/pokemon/missingno")).andExpect(method(GET))
                .andRespond(withResourceNotFound());

        Optional<PokemonDetail> detail = adapter.findDetail(PokemonIdentifier.of("missingno"));

        assertThat(detail).isEmpty();
    }

    @Test
    void upstreamFailureBecomesExternalServiceException() {
        server.expect(once(), requestTo(BASE + "/pokemon/1"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(() -> adapter.findDetail(PokemonIdentifier.of("1")))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void failureWhileEnrichingAPageIsPropagated() throws IOException {
        expect("/pokemon?offset=0&limit=1", "page-0-1.json");
        server.expect(once(), requestTo(BASE + "/pokemon/bulbasaur"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThatThrownBy(() -> adapter.list(new PageQuery(0, 1)))
                .isInstanceOf(ExternalServiceException.class);
    }

    private void expect(String path, String fixture) throws IOException {
        String body = new ClassPathResource("pokeapi/" + fixture).getContentAsString(StandardCharsets.UTF_8);
        server.expect(once(), requestTo(BASE + path))
                .andExpect(method(GET))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }
}
