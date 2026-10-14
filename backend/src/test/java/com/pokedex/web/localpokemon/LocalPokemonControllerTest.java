package com.pokedex.web.localpokemon;

import com.pokedex.TestData;
import com.pokedex.application.localpokemon.DeleteLocalPokemonUseCase;
import com.pokedex.application.localpokemon.GetLocalPokemonUseCase;
import com.pokedex.application.localpokemon.ListLocalPokemonUseCase;
import com.pokedex.application.localpokemon.RefreshLocalPokemonUseCase;
import com.pokedex.application.localpokemon.SyncPokemonUseCase;
import com.pokedex.application.localpokemon.UpdateLocalPokemonCommand;
import com.pokedex.application.localpokemon.UpdateLocalPokemonUseCase;
import com.pokedex.domain.exception.ConflictException;
import com.pokedex.domain.exception.NotFoundException;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import com.pokedex.web.WebTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocalPokemonController.class)
@Import(WebTestConfig.class)
class LocalPokemonControllerTest {

    private static final RequestPostProcessor USER = jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"));
    private static final RequestPostProcessor ADMIN = jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ListLocalPokemonUseCase listLocal;
    @MockitoBean
    private GetLocalPokemonUseCase getLocal;
    @MockitoBean
    private SyncPokemonUseCase syncPokemon;
    @MockitoBean
    private RefreshLocalPokemonUseCase refreshLocal;
    @MockitoBean
    private UpdateLocalPokemonUseCase updateLocal;
    @MockitoBean
    private DeleteLocalPokemonUseCase deleteLocal;

    @Test
    void requiresAuthentication() throws Exception {
        mvc.perform(get("/api/local-pokemon"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(jsonPath("$.status").value(401));
        verifyNoInteractions(listLocal);
    }

    @Test
    void listReturnsStoredPokemonWithCustomFields() throws Exception {
        when(listLocal.execute(new PageQuery(0, 20), "bul"))
                .thenReturn(new PageResult<>(List.of(TestData.localBulbasaur(3L)), 0, 20, 1));

        mvc.perform(get("/api/local-pokemon").param("search", "bul").with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].localizedName").value("フシギダネ"))
                .andExpect(jsonPath("$.content[0].tags[0]").value("starter"))
                .andExpect(jsonPath("$.content[0].version").value(3));
    }

    @Test
    void getUnknownReturns404() throws Exception {
        when(getLocal.execute(99)).thenThrow(new NotFoundException("Local Pokemon", 99));

        mvc.perform(get("/api/local-pokemon/99").with(USER))
                .andExpect(status().isNotFound());
    }

    @Test
    void syncReturns201WithLocation() throws Exception {
        when(syncPokemon.execute("bulbasaur")).thenReturn(LocalPokemon.fromCatalog(TestData.bulbasaurDetail(),
                TestData.NOW));

        mvc.perform(post("/api/local-pokemon").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idOrName\":\"bulbasaur\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/local-pokemon/1"))
                .andExpect(jsonPath("$.name").value("bulbasaur"));
    }

    @Test
    void syncWithBlankIdentifierReturns400() throws Exception {
        mvc.perform(post("/api/local-pokemon").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idOrName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.idOrName").exists());
        verifyNoInteractions(syncPokemon);
    }

    @Test
    void syncOfAlreadyStoredPokemonReturns409() throws Exception {
        when(syncPokemon.execute("1")).thenThrow(new ConflictException("already stored"));

        mvc.perform(post("/api/local-pokemon").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idOrName\":\"1\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mvc.perform(put("/api/local-pokemon/1").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"localizedName\": "))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(updateLocal);
    }

    @Test
    void wrongJsonTypeReturns400() throws Exception {
        mvc.perform(put("/api/local-pokemon/1").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tags\": 5, \"version\": \"abc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateMapsRequestToCommand() throws Exception {
        when(updateLocal.execute(any())).thenReturn(TestData.localBulbasaur(4L));

        mvc.perform(put("/api/local-pokemon/1").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"localizedName":"Bisasam","region":"Kanto","tags":["starter"],"notes":"n","version":3}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(4));

        verify(updateLocal).execute(new UpdateLocalPokemonCommand(1, "Bisasam", "Kanto", Set.of("starter"), "n", 3L));
    }

    @Test
    void updateWithDomainViolationReturns400WithFieldErrors() throws Exception {
        when(updateLocal.execute(any())).thenThrow(ValidationException.of("tags", "bad tag"));

        mvc.perform(put("/api/local-pokemon/1").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tags\":[\"NOT VALID\"]}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"))
                .andExpect(jsonPath("$.errors.tags").value("bad tag"));
    }

    @Test
    void updateUnknownReturns404() throws Exception {
        when(updateLocal.execute(any())).thenThrow(new NotFoundException("Local Pokemon", 42));

        mvc.perform(put("/api/local-pokemon/42").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void concurrentModificationReturns409() throws Exception {
        when(updateLocal.execute(any()))
                .thenThrow(new ObjectOptimisticLockingFailureException("LocalPokemonEntity", 1));

        mvc.perform(put("/api/local-pokemon/1").with(USER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict());
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mvc.perform(get("/api/local-pokemon/pikachu").with(USER))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshReturnsUpdatedPokemon() throws Exception {
        when(refreshLocal.execute(1)).thenReturn(TestData.localBulbasaur(5L));

        mvc.perform(post("/api/local-pokemon/1/refresh").with(USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value(5));
    }

    @Test
    void deleteIsForbiddenForRegularUsers() throws Exception {
        mvc.perform(delete("/api/local-pokemon/1").with(USER))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verifyNoInteractions(deleteLocal);
    }

    @Test
    void adminCanDelete() throws Exception {
        mvc.perform(delete("/api/local-pokemon/1").with(ADMIN))
                .andExpect(status().isNoContent());
        verify(deleteLocal).execute(1);
    }

    @Test
    void deleteUnknownReturns404() throws Exception {
        doThrow(new NotFoundException("Local Pokemon", 2)).when(deleteLocal).execute(2);

        mvc.perform(delete("/api/local-pokemon/2").with(ADMIN))
                .andExpect(status().isNotFound());
    }
}
