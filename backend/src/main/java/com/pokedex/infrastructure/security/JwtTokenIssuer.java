package com.pokedex.infrastructure.security;

import com.pokedex.application.port.TokenIssuer;
import com.pokedex.domain.model.AuthToken;
import com.pokedex.domain.model.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

/**
 * Issues HS256-signed JWTs. The {@code roles} claim is read back by the resource server to build authorities.
 */
public class JwtTokenIssuer implements TokenIssuer {

    static final String ROLES_CLAIM = "roles";

    private final JwtEncoder encoder;
    private final JwtProperties properties;
    private final Clock clock;

    public JwtTokenIssuer(JwtEncoder encoder, JwtProperties properties, Clock clock) {
        this.encoder = encoder;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public AuthToken issue(User user) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.ttl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.username())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim(ROLES_CLAIM, List.of(user.role().name()))
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthToken(token, expiresAt);
    }
}
