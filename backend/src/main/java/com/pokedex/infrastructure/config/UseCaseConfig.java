package com.pokedex.infrastructure.config;

import com.pokedex.application.auth.GetCurrentUserUseCase;
import com.pokedex.application.auth.LoginUseCase;
import com.pokedex.application.auth.RegisterUserUseCase;
import com.pokedex.application.catalog.GetPokemonDetailUseCase;
import com.pokedex.application.catalog.ListPokemonUseCase;
import com.pokedex.application.localpokemon.DeleteLocalPokemonUseCase;
import com.pokedex.application.localpokemon.GetLocalPokemonUseCase;
import com.pokedex.application.localpokemon.ListLocalPokemonUseCase;
import com.pokedex.application.localpokemon.RefreshLocalPokemonUseCase;
import com.pokedex.application.localpokemon.SyncPokemonUseCase;
import com.pokedex.application.localpokemon.UpdateLocalPokemonUseCase;
import com.pokedex.application.port.LocalPokemonRepository;
import com.pokedex.application.port.PasswordHasher;
import com.pokedex.application.port.PokemonCatalog;
import com.pokedex.application.port.TokenIssuer;
import com.pokedex.application.port.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Composition root: the application layer has no Spring annotations, so its use cases are wired here.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    ListPokemonUseCase listPokemonUseCase(PokemonCatalog catalog) {
        return new ListPokemonUseCase(catalog);
    }

    @Bean
    GetPokemonDetailUseCase getPokemonDetailUseCase(PokemonCatalog catalog) {
        return new GetPokemonDetailUseCase(catalog);
    }

    @Bean
    SyncPokemonUseCase syncPokemonUseCase(PokemonCatalog catalog, LocalPokemonRepository repository, Clock clock) {
        return new SyncPokemonUseCase(catalog, repository, clock);
    }

    @Bean
    RefreshLocalPokemonUseCase refreshLocalPokemonUseCase(PokemonCatalog catalog, LocalPokemonRepository repository,
                                                          Clock clock) {
        return new RefreshLocalPokemonUseCase(catalog, repository, clock);
    }

    @Bean
    ListLocalPokemonUseCase listLocalPokemonUseCase(LocalPokemonRepository repository) {
        return new ListLocalPokemonUseCase(repository);
    }

    @Bean
    GetLocalPokemonUseCase getLocalPokemonUseCase(LocalPokemonRepository repository) {
        return new GetLocalPokemonUseCase(repository);
    }

    @Bean
    UpdateLocalPokemonUseCase updateLocalPokemonUseCase(LocalPokemonRepository repository, Clock clock) {
        return new UpdateLocalPokemonUseCase(repository, clock);
    }

    @Bean
    DeleteLocalPokemonUseCase deleteLocalPokemonUseCase(LocalPokemonRepository repository) {
        return new DeleteLocalPokemonUseCase(repository);
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepository users, PasswordHasher hasher, Clock clock) {
        return new RegisterUserUseCase(users, hasher, clock);
    }

    @Bean
    LoginUseCase loginUseCase(UserRepository users, PasswordHasher hasher, TokenIssuer tokenIssuer) {
        return new LoginUseCase(users, hasher, tokenIssuer);
    }

    @Bean
    GetCurrentUserUseCase getCurrentUserUseCase(UserRepository users) {
        return new GetCurrentUserUseCase(users);
    }
}
