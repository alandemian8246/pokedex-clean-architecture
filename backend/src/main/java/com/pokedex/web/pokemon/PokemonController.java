package com.pokedex.web.pokemon;

import com.pokedex.application.catalog.GetPokemonDetailUseCase;
import com.pokedex.application.catalog.ListPokemonUseCase;
import com.pokedex.domain.model.PageQuery;
import com.pokedex.web.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pokemon")
@Tag(name = "Pokemon catalog", description = "Public read-only data sourced from PokeAPI")
public class PokemonController {

    private final ListPokemonUseCase listPokemon;
    private final GetPokemonDetailUseCase getPokemonDetail;

    public PokemonController(ListPokemonUseCase listPokemon, GetPokemonDetailUseCase getPokemonDetail) {
        this.listPokemon = listPokemon;
        this.getPokemonDetail = getPokemonDetail;
    }

    @GetMapping
    @Operation(summary = "US01 - paginated list with sprite, category, weight and abilities")
    public PageResponse<PokemonSummaryResponse> list(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(listPokemon.execute(new PageQuery(page, size)), PokemonSummaryResponse::from);
    }

    @GetMapping("/{idOrName}")
    @Operation(summary = "US02 - detail with artwork, stats, description and evolution chain")
    public PokemonDetailResponse get(@PathVariable String idOrName) {
        return PokemonDetailResponse.from(getPokemonDetail.execute(idOrName));
    }
}
