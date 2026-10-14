package com.pokedex.web.localpokemon;

import com.pokedex.application.localpokemon.DeleteLocalPokemonUseCase;
import com.pokedex.application.localpokemon.GetLocalPokemonUseCase;
import com.pokedex.application.localpokemon.ListLocalPokemonUseCase;
import com.pokedex.application.localpokemon.RefreshLocalPokemonUseCase;
import com.pokedex.application.localpokemon.SyncPokemonUseCase;
import com.pokedex.application.localpokemon.UpdateLocalPokemonCommand;
import com.pokedex.application.localpokemon.UpdateLocalPokemonUseCase;
import com.pokedex.domain.model.LocalPokemon;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.web.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/local-pokemon")
@Tag(name = "Local Pokedex", description = "Replicated Pokemon with proprietary fields (requires authentication)")
@SecurityRequirement(name = "bearerAuth")
public class LocalPokemonController {

    private final ListLocalPokemonUseCase listLocal;
    private final GetLocalPokemonUseCase getLocal;
    private final SyncPokemonUseCase syncPokemon;
    private final RefreshLocalPokemonUseCase refreshLocal;
    private final UpdateLocalPokemonUseCase updateLocal;
    private final DeleteLocalPokemonUseCase deleteLocal;

    @SuppressWarnings("java:S107")
    public LocalPokemonController(ListLocalPokemonUseCase listLocal, GetLocalPokemonUseCase getLocal,
                                  SyncPokemonUseCase syncPokemon, RefreshLocalPokemonUseCase refreshLocal,
                                  UpdateLocalPokemonUseCase updateLocal, DeleteLocalPokemonUseCase deleteLocal) {
        this.listLocal = listLocal;
        this.getLocal = getLocal;
        this.syncPokemon = syncPokemon;
        this.refreshLocal = refreshLocal;
        this.updateLocal = updateLocal;
        this.deleteLocal = deleteLocal;
    }

    @GetMapping
    @Operation(summary = "List locally stored Pokemon, optionally filtered by name or localized name")
    public PageResponse<LocalPokemonResponse> list(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String search) {
        return PageResponse.from(listLocal.execute(new PageQuery(page, size), search), LocalPokemonResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one locally stored Pokemon")
    public LocalPokemonResponse get(@PathVariable int id) {
        return LocalPokemonResponse.from(getLocal.execute(id));
    }

    @PostMapping
    @Operation(summary = "US03 - replicate a Pokemon from PokeAPI into the local store")
    public ResponseEntity<LocalPokemonResponse> sync(@Valid @RequestBody SyncPokemonRequest request) {
        LocalPokemon created = syncPokemon.execute(request.idOrName());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.id()).toUri();
        return ResponseEntity.created(location).body(LocalPokemonResponse.from(created));
    }

    @PostMapping("/{id}/refresh")
    @Operation(summary = "US03 - re-pull upstream data, keeping proprietary fields")
    public LocalPokemonResponse refresh(@PathVariable int id) {
        return LocalPokemonResponse.from(refreshLocal.execute(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "US04 - update the proprietary fields of a locally stored Pokemon")
    public LocalPokemonResponse update(@PathVariable int id, @RequestBody UpdateLocalPokemonRequest request) {
        UpdateLocalPokemonCommand command = new UpdateLocalPokemonCommand(id, request.localizedName(),
                request.region(), request.tags(), request.notes(), request.version());
        return LocalPokemonResponse.from(updateLocal.execute(command));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a Pokemon from the local store (ADMIN only)")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        deleteLocal.execute(id);
        return ResponseEntity.noContent().build();
    }
}
