package com.pokedex.infrastructure.pokeapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "pokeapi")
public record PokeApiProperties(String baseUrl, Duration connectTimeout, Duration readTimeout) {
}
