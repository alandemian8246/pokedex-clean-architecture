package com.pokedex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonIdentifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end flow through every layer (HTTP, security, use cases, JPA, Flyway) with only PokeAPI stubbed.
 */
@SpringBootTest(properties = "app.seed.demo-users=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PokedexApplicationIntegrationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PokemonCatalog catalog;

    @Test
    void registerLoginSyncUpdateAndDelete() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"brock\",\"password\":\"Onix12345\"}"))
                .andExpect(status().isCreated());
        String userToken = login("brock", "Onix12345");

        PokemonDetail mewtwo = withId(150, "mewtwo");
        when(catalog.findDetail(new PokemonIdentifier("mewtwo"))).thenReturn(Optional.of(mewtwo));

        mvc.perform(post("/api/local-pokemon").header(HttpHeaders.AUTHORIZATION, userToken)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"idOrName\":\"mewtwo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(150))
                .andExpect(jsonPath("$.version").value(0));

        mvc.perform(put("/api/local-pokemon/150").header(HttpHeaders.AUTHORIZATION, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"localizedName\":\"ミュウツー\",\"tags\":[\"Legendary\"],\"version\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags[0]").value("legendary"))
                .andExpect(jsonPath("$.version").value(1));

        mvc.perform(put("/api/local-pokemon/150").header(HttpHeaders.AUTHORIZATION, userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"localizedName\":\"stale\",\"version\":0}"))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/local-pokemon/150").header(HttpHeaders.AUTHORIZATION, userToken))
                .andExpect(status().isForbidden());

        String adminToken = login("admin", "Admin1234");
        mvc.perform(delete("/api/local-pokemon/150").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/local-pokemon/150").header(HttpHeaders.AUTHORIZATION, adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void seededDataIsAvailableToDemoUser() throws Exception {
        String token = login("demo", "Demo1234");

        mvc.perform(get("/api/local-pokemon").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("bulbasaur"))
                .andExpect(jsonPath("$.content[0].localizedName").value("フシギダネ"));
    }

    @Test
    void tamperedTokenIsRejected() throws Exception {
        String token = login("demo", "Demo1234");

        mvc.perform(get("/api/local-pokemon").header(HttpHeaders.AUTHORIZATION, token + "x"))
                .andExpect(status().isUnauthorized());
    }

    private String login(String username, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"%s\"}".formatted(username, password)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return "Bearer " + json.get("accessToken").asText();
    }

    private static PokemonDetail withId(int id, String name) {
        PokemonDetail base = TestData.bulbasaurDetail();
        return new PokemonDetail(id, name, "Genetic Pokémon", 20, 1220, base.spriteUrl(), base.artworkUrl(),
                base.types(), base.abilities(), base.stats(), base.description(), null);
    }
}
