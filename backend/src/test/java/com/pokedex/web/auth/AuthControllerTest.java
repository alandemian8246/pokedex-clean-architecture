package com.pokedex.web.auth;

import com.pokedex.application.auth.AuthenticatedUser;
import com.pokedex.application.auth.GetCurrentUserUseCase;
import com.pokedex.application.auth.LoginUseCase;
import com.pokedex.application.auth.RegisterUserUseCase;
import com.pokedex.domain.exception.ConflictException;
import com.pokedex.domain.exception.InvalidCredentialsException;
import com.pokedex.domain.exception.ValidationException;
import com.pokedex.domain.model.AuthToken;
import com.pokedex.domain.model.Role;
import com.pokedex.domain.model.User;
import com.pokedex.web.WebTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static com.pokedex.TestData.NOW;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(WebTestConfig.class)
class AuthControllerTest {

    private static final User ASH = new User(1L, "ash", "hash", Role.USER, NOW);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private RegisterUserUseCase registerUser;
    @MockitoBean
    private LoginUseCase login;
    @MockitoBean
    private GetCurrentUserUseCase currentUser;

    @Test
    void registerReturns201WithoutPasswordHash() throws Exception {
        when(registerUser.execute("ash", "Pikachu25")).thenReturn(ASH);

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ash\",\"password\":\"Pikachu25\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ash"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void registerWithMissingFieldsReturns400() throws Exception {
        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ash\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").exists());
        verifyNoInteractions(registerUser);
    }

    @Test
    void registerWithWeakPasswordReturns400() throws Exception {
        when(registerUser.execute("ash", "weak")).thenThrow(ValidationException.of("password", "too short"));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ash\",\"password\":\"weak\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.password").value("too short"));
    }

    @Test
    void registerDuplicateReturns409() throws Exception {
        when(registerUser.execute("ash", "Pikachu25")).thenThrow(new ConflictException("taken"));

        mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ash\",\"password\":\"Pikachu25\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void loginReturnsBearerToken() throws Exception {
        when(login.execute("ash", "Pikachu25"))
                .thenReturn(new AuthenticatedUser(ASH, new AuthToken("jwt-value", NOW.plusSeconds(3600))));

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ash\",\"password\":\"Pikachu25\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-value"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.username").value("ash"));
    }

    @Test
    void loginWithBadCredentialsReturns401() throws Exception {
        when(login.execute("ash", "nope")).thenThrow(new InvalidCredentialsException());

        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"ash\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Invalid username or password"));
    }

    @Test
    void meRequiresToken() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsAuthenticatedUser() throws Exception {
        when(currentUser.execute("ash")).thenReturn(ASH);

        mvc.perform(get("/api/auth/me").with(jwt().jwt(token -> token.subject("ash"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ash"));
    }
}
