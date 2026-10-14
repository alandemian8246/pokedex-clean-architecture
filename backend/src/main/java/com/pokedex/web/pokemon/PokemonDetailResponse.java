package com.pokedex.web.pokemon;

import com.pokedex.domain.model.EvolutionNode;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.Stat;

import java.util.List;

public record PokemonDetailResponse(
        int id,
        String name,
        String category,
        double heightM,
        double weightKg,
        String spriteUrl,
        String artworkUrl,
        List<String> types,
        List<String> abilities,
        List<StatResponse> stats,
        String description,
        EvolutionResponse evolutionChain) {

    public record StatResponse(String name, int value) {
        static StatResponse from(Stat stat) {
            return new StatResponse(stat.name(), stat.value());
        }
    }

    public record EvolutionResponse(int id, String name, String spriteUrl, String trigger,
                                    List<EvolutionResponse> evolvesTo) {
        static EvolutionResponse from(EvolutionNode node) {
            return node == null ? null : new EvolutionResponse(node.id(), node.name(), node.spriteUrl(),
                    node.trigger(), node.evolvesTo().stream().map(EvolutionResponse::from).toList());
        }
    }

    static PokemonDetailResponse from(PokemonDetail detail) {
        return new PokemonDetailResponse(
                detail.id(),
                detail.name(),
                detail.category(),
                detail.height() / 10.0,
                detail.weight() / 10.0,
                detail.spriteUrl(),
                detail.artworkUrl(),
                detail.types(),
                detail.abilities(),
                detail.stats().stream().map(StatResponse::from).toList(),
                detail.description(),
                EvolutionResponse.from(detail.evolutionChain()));
    }
}
