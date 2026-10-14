package com.pokedex.web.pokemon;

import com.pokedex.TestData;
import com.pokedex.application.catalog.GetPokemonDetailUseCase;
import com.pokedex.application.catalog.ListPokemonUseCase;
import com.pokedex.domain.exception.ExternalServiceException;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import com.pokedex.web.WebTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PokemonController.class)
@Import(WebTestConfig.class)
class PokemonControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListPokemonUseCase listPokemon;
    @MockitoBean
    private GetPokemonDetailUseCase getPokemonDetail;

    @Test
    void listIsPublicAndReturnsPageEnvelope() throws Exception {
        when(listPokemon.execute(new PageQuery(0, 20)))
                .thenReturn(new PageResult<>(List.of(TestData.bulbasaurSummary()), 0, 20, 1302));

        mvc.perform(get("/api/pokemon"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
                .andExpect(jsonPath("$.content[0].category").value("Seed Pokémon"))
                .andExpect(jsonPath("$.content[0].weightKg").value(6.9))
                .andExpect(jsonPath("$.content[0].abilities[0]").value("overgrow"))
                .andExpect(jsonPath("$.totalElements").value(1302))
                .andExpect(jsonPath("$.totalPages").value(66));
    }

    @Test
    void invalidPaginationReturns400WithFieldErrors() throws Exception {
        mvc.perform(get("/api/pokemon").param("size", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/problem+json"))
                .andExpect(jsonPath("$.errors.size").exists());
    }

    @Test
    void nonNumericPageReturns400() throws Exception {
        mvc.perform(get("/api/pokemon").param("page", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void detailReturnsStatsDescriptionAndEvolutionChain() throws Exception {
        when(getPokemonDetail.execute("bulbasaur")).thenReturn(TestData.bulbasaurDetail());

        mvc.perform(get("/api/pokemon/bulbasaur"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artworkUrl").value("artwork-1.png"))
                .andExpect(jsonPath("$.heightM").value(0.7))
                .andExpect(jsonPath("$.stats[0].name").value("hp"))
                .andExpect(jsonPath("$.description").isNotEmpty())
                .andExpect(jsonPath("$.evolutionChain.evolvesTo[0].name").value("ivysaur"))
                .andExpect(jsonPath("$.evolutionChain.evolvesTo[0].evolvesTo[0].trigger").value("Level 32"));
    }

    @Test
    void unknownPokemonReturns404Problem() throws Exception {
        when(getPokemonDetail.execute("missingno")).thenThrow(new NotFoundException("Pokemon", "missingno"));

        mvc.perform(get("/api/pokemon/missingno"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Pokemon 'missingno' was not found"));
    }

    @Test
    void malformedIdentifierReturns400() throws Exception {
        when(getPokemonDetail.execute(any())).thenThrow(ValidationException.of("idOrName", "invalid"));

        mvc.perform(get("/api/pokemon/bad name"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idOrName").value("invalid"));
    }

    @Test
    void upstreamOutageReturns502() throws Exception {
        when(getPokemonDetail.execute("1")).thenThrow(new ExternalServiceException("PokeAPI is currently unavailable", null));

        mvc.perform(get("/api/pokemon/1"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Upstream service error"));
    }

    @Test
    void unexpectedErrorsDoNotLeakDetails() throws Exception {
        when(getPokemonDetail.execute("1")).thenThrow(new IllegalStateException("db password is hunter2"));

        mvc.perform(get("/api/pokemon/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }
}
