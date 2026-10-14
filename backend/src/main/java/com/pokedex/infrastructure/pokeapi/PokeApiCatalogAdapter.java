package com.pokedex.infrastructure.pokeapi;

import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.domain.exception.ExternalServiceException;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.domain.model.PageResult;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonIdentifier;
import com.pokedex.domain.model.PokemonSummary;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.EvolutionChain;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.NamedResource;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.Page;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.Pokemon;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.Species;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

/**
 * {@link PokemonCatalog} backed by PokeAPI. PokeAPI's list endpoint only returns names, so each entry of a page
 * is enriched (pokemon + species) concurrently; responses are cached by {@link PokeApiClient}.
 */
public class PokeApiCatalogAdapter implements PokemonCatalog {

    private final PokeApiClient client;
    private final Executor executor;

    public PokeApiCatalogAdapter(PokeApiClient client, Executor executor) {
        this.client = client;
        this.executor = executor;
    }

    @Override
    public PageResult<PokemonSummary> list(PageQuery query) {
        Page page = client.getPage(query.offset(), query.size())
                .orElseThrow(() -> new ExternalServiceException("PokeAPI returned no Pokemon list", null));

        List<CompletableFuture<Optional<PokemonSummary>>> futures = page.results().stream()
                .map(entry -> CompletableFuture.supplyAsync(() -> summarize(entry), executor))
                .toList();

        List<PokemonSummary> items = futures.stream()
                .map(PokeApiCatalogAdapter::join)
                .flatMap(Optional::stream)
                .toList();

        return new PageResult<>(items, query.page(), query.size(), page.count());
    }

    @Override
    public Optional<PokemonDetail> findDetail(PokemonIdentifier identifier) {
        return client.getPokemon(identifier.value()).map(pokemon -> {
            Species species = speciesOf(pokemon).orElse(null);
            EvolutionChain chain = species == null || species.evolutionChain() == null ? null
                    : PokeApiMapper.idFromUrl(species.evolutionChain().url())
                            .flatMap(client::getEvolutionChain)
                            .orElse(null);
            return PokeApiMapper.toDetail(pokemon, species, chain);
        });
    }

    private Optional<PokemonSummary> summarize(NamedResource entry) {
        return client.getPokemon(entry.name())
                .map(pokemon -> PokeApiMapper.toSummary(pokemon, speciesOf(pokemon).orElse(null)));
    }

    private Optional<Species> speciesOf(Pokemon pokemon) {
        return Optional.ofNullable(pokemon.species())
                .map(NamedResource::url)
                .flatMap(PokeApiMapper::idFromUrl)
                .flatMap(client::getSpecies);
    }

    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw new ExternalServiceException("PokeAPI request failed", Objects.requireNonNullElse(e.getCause(), e));
        }
    }
}
