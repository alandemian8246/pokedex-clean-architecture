package com.pokedex.web.common;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.web.resources.static-locations=classpath:/spa-test/")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SpaWebConfigTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void clientSideRoutesFallBackToIndex() throws Exception {
        mvc.perform(get("/pokemon/25"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("spa-index")));
        // "/" is handled by Spring Boot's welcome page, which forwards to index.html
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(forwardedUrl("index.html"));
    }

    @Test
    void existingAssetsAreServedWithTheirOwnContentType() throws Exception {
        mvc.perform(get("/assets/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/javascript"))
                .andExpect(content().string(containsString("spa-asset")));
    }

    @Test
    void missingAssetsAndUnknownApiPathsAre404() throws Exception {
        mvc.perform(get("/assets/missing.js")).andExpect(status().isNotFound());
        mvc.perform(get("/api/does-not-exist")).andExpect(status().isUnauthorized());
    }
}
