package com.pokedex.web;

import com.pokedex.infrastructure.security.SecurityConfig;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.time.Clock;

/**
 * Brings the real security rules into {@code @WebMvcTest} slices so authorization is tested per endpoint.
 */
@TestConfiguration
@Import(SecurityConfig.class)
public class WebTestConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
