package com.pokedex.infrastructure.security;

import com.pokedex.domain.model.AuthToken;
import com.pokedex.domain.model.Role;
import com.pokedex.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenIssuerTest {

    private static final JwtProperties PROPERTIES =
            new JwtProperties("test-secret-test-secret-test-secret-123", Duration.ofHours(1), "pokedex-api");
    private final SecurityConfig config = new SecurityConfig();
    private final JwtDecoder decoder = config.jwtDecoder(PROPERTIES);

    @Test
    void issuedTokenIsSignedAndCarriesSubjectAndRoles() {
        Instant now = Instant.now();
        JwtTokenIssuer issuer = new JwtTokenIssuer(config.jwtEncoder(PROPERTIES), PROPERTIES,
                Clock.fixed(now, ZoneOffset.UTC));

        AuthToken token = issuer.issue(new User(1L, "ash", "hash", Role.ADMIN, now));
        Jwt jwt = decoder.decode(token.value());

        assertThat(jwt.getSubject()).isEqualTo("ash");
        assertThat(jwt.getClaimAsStringList("roles")).isEqualTo(List.of("ADMIN"));
        assertThat(token.expiresAt()).isEqualTo(now.plus(Duration.ofHours(1)));
    }

    @Test
    void expiredTokenIsRejected() {
        Instant longAgo = Instant.now().minus(Duration.ofDays(1));
        JwtTokenIssuer issuer = new JwtTokenIssuer(config.jwtEncoder(PROPERTIES), PROPERTIES,
                Clock.fixed(longAgo, ZoneOffset.UTC));

        String token = issuer.issue(new User(1L, "ash", "hash", Role.USER, longAgo)).value();

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtValidationException.class);
    }

    @Test
    void rejectsWeakSecretAndInvalidTtl() {
        assertThatThrownBy(() -> new JwtProperties("short", Duration.ofHours(1), "x"))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new JwtProperties("x".repeat(32), Duration.ZERO, "x"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bcryptHasherHashesAndVerifies() {
        BCryptPasswordHasher hasher = new BCryptPasswordHasher(new BCryptPasswordEncoder(4));

        String hash = hasher.hash("Pikachu25");

        assertThat(hash).isNotEqualTo("Pikachu25");
        assertThat(hasher.matches("Pikachu25", hash)).isTrue();
        assertThat(hasher.matches("wrong", hash)).isFalse();
    }
}
