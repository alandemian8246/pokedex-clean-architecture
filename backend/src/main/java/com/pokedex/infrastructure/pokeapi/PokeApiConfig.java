package com.pokedex.infrastructure.pokeapi;

import com.pokedex.application.port.PokemonCatalog;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableCaching
@EnableConfigurationProperties(PokeApiProperties.class)
public class PokeApiConfig {

    @Bean
    PokeApiClient pokeApiClient(RestClient.Builder builder, PokeApiProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.connectTimeout());
        requestFactory.setReadTimeout(properties.readTimeout());

        RestClient restClient = builder
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .defaultHeader("User-Agent", "pokedex-api")
                .build();
        return new PokeApiClient(restClient);
    }

    @Bean(destroyMethod = "close")
    ExecutorService pokeApiExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    PokemonCatalog pokemonCatalog(PokeApiClient client, ExecutorService pokeApiExecutor) {
        return new PokeApiCatalogAdapter(client, pokeApiExecutor);
    }
}
