package com.pokedex.infrastructure.pokeapi;

import com.pokedex.domain.exception.ExternalServiceException;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.EvolutionChain;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.Page;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.Pokemon;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.Species;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Optional;

/**
 * Thin HTTP client for PokeAPI. Every call is cached (Caffeine) because PokeAPI data is effectively static,
 * which keeps the paginated listing fast and respects PokeAPI's fair-use policy.
 * A 404 from PokeAPI becomes an empty Optional; any other failure becomes an {@link ExternalServiceException}.
 */
public class PokeApiClient {

    public static final String PAGES_CACHE = "pokeapi-pages";
    public static final String POKEMON_CACHE = "pokeapi-pokemon";
    public static final String SPECIES_CACHE = "pokeapi-species";
    public static final String EVOLUTION_CACHE = "pokeapi-evolution-chains";

    private static final Logger log = LoggerFactory.getLogger(PokeApiClient.class);

    private final RestClient restClient;

    public PokeApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    @Cacheable(cacheNames = PAGES_CACHE, key = "#offset + ':' + #limit")
    public Optional<Page> getPage(int offset, int limit) {
        return get("/pokemon?offset={offset}&limit={limit}", Page.class, offset, limit);
    }

    @Cacheable(cacheNames = POKEMON_CACHE)
    public Optional<Pokemon> getPokemon(String idOrName) {
        return get("/pokemon/{idOrName}", Pokemon.class, idOrName);
    }

    @Cacheable(cacheNames = SPECIES_CACHE)
    public Optional<Species> getSpecies(int id) {
        return get("/pokemon-species/{id}", Species.class, id);
    }

    @Cacheable(cacheNames = EVOLUTION_CACHE)
    public Optional<EvolutionChain> getEvolutionChain(int id) {
        return get("/evolution-chain/{id}", EvolutionChain.class, id);
    }

    private <T> Optional<T> get(String uriTemplate, Class<T> type, Object... variables) {
        try {
            return Optional.ofNullable(restClient.get().uri(uriTemplate, variables).retrieve().body(type));
        } catch (HttpClientErrorException.NotFound notFound) {
            return Optional.empty();
        } catch (RestClientException e) {
            log.warn("PokeAPI call {} failed: {}", uriTemplate, e.getMessage());
            throw new ExternalServiceException("PokeAPI is currently unavailable", e);
        }
    }
}
