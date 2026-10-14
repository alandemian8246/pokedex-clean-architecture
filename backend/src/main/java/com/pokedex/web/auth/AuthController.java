package com.pokedex.web.auth;

import com.pokedex.application.auth.AuthenticatedUser;
import com.pokedex.application.auth.GetCurrentUserUseCase;
import com.pokedex.application.auth.LoginUseCase;
import com.pokedex.application.auth.RegisterUserUseCase;
import com.pokedex.domain.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and JWT login")
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final LoginUseCase login;
    private final GetCurrentUserUseCase currentUser;

    public AuthController(RegisterUserUseCase registerUser, LoginUseCase login, GetCurrentUserUseCase currentUser) {
        this.registerUser = registerUser;
        this.login = login;
        this.currentUser = currentUser;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a USER account")
    public UserResponse register(@Valid @RequestBody CredentialsRequest request) {
        return UserResponse.from(registerUser.execute(request.username(), request.password()));
    }

    @PostMapping("/login")
    @Operation(summary = "Exchange credentials for a bearer token")
    public TokenResponse login(@Valid @RequestBody CredentialsRequest request) {
        AuthenticatedUser authenticated = login.execute(request.username(), request.password());
        return new TokenResponse(authenticated.token().value(), "Bearer", authenticated.token().expiresAt(),
                UserResponse.from(authenticated.user()));
    }

    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Profile of the authenticated user")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        User user = currentUser.execute(jwt.getSubject());
        return UserResponse.from(user);
    }
}
