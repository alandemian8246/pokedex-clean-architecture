package com.pokedex.domain.model;

import java.util.List;

/**
 * One stage of an evolution chain. Branching chains (e.g. Eevee) have several {@code evolvesTo} children.
 *
 * @param trigger human readable condition to reach this stage from its parent (null for the base stage)
 */
public record EvolutionNode(int id, String name, String spriteUrl, String trigger, List<EvolutionNode> evolvesTo) {

    public EvolutionNode {
        evolvesTo = evolvesTo == null ? List.of() : List.copyOf(evolvesTo);
    }
}
