package com.pokedex.infrastructure.pokeapi;

import com.pokedex.domain.model.EvolutionNode;
import com.pokedex.domain.model.PokemonDetail;
import com.pokedex.domain.model.PokemonSummary;
import com.pokedex.domain.model.Stat;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.AbilitySlot;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.ChainLink;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.EvolutionChain;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.EvolutionDetail;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.NamedResource;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.Pokemon;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.Species;
import com.pokedex.infrastructure.pokeapi.PokeApiModels.TypeSlot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Translates PokeAPI payloads into domain objects. Pure functions, no I/O.
 */
public final class PokeApiMapper {

    static final String SPRITE_URL = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/%d.png";
    private static final String ENGLISH = "en";
    private static final Pattern TRAILING_ID = Pattern.compile("/(\\d+)/?$");
    private static final Pattern CONTROL_WHITESPACE = Pattern.compile("[\\f\\n\\r\\u00ad\\s]+");

    private PokeApiMapper() {
    }

    public static PokemonSummary toSummary(Pokemon pokemon, Species species) {
        return new PokemonSummary(
                pokemon.id(),
                pokemon.name(),
                category(species),
                pokemon.weight(),
                sprite(pokemon),
                types(pokemon),
                abilities(pokemon));
    }

    public static PokemonDetail toDetail(Pokemon pokemon, Species species, EvolutionChain chain) {
        return new PokemonDetail(
                pokemon.id(),
                pokemon.name(),
                category(species),
                pokemon.height(),
                pokemon.weight(),
                sprite(pokemon),
                artwork(pokemon),
                types(pokemon),
                abilities(pokemon),
                stats(pokemon),
                description(species),
                chain == null || chain.chain() == null ? null : toEvolutionNode(chain.chain()));
    }

    /** Extracts the numeric id from resource URLs such as {@code https://pokeapi.co/api/v2/pokemon-species/1/}. */
    public static Optional<Integer> idFromUrl(String url) {
        if (url == null) {
            return Optional.empty();
        }
        Matcher matcher = TRAILING_ID.matcher(url);
        return matcher.find() ? Optional.of(Integer.parseInt(matcher.group(1))) : Optional.empty();
    }

    static String category(Species species) {
        if (species == null || species.genera() == null) {
            return null;
        }
        return species.genera().stream()
                .filter(genus -> isEnglish(genus.language()))
                .map(PokeApiModels.Genus::genus)
                .findFirst()
                .orElse(null);
    }

    static String description(Species species) {
        if (species == null || species.flavorTextEntries() == null) {
            return null;
        }
        return species.flavorTextEntries().stream()
                .filter(entry -> isEnglish(entry.language()) && entry.flavorText() != null)
                .map(entry -> CONTROL_WHITESPACE.matcher(entry.flavorText()).replaceAll(" ").strip())
                .findFirst()
                .orElse(null);
    }

    static String describeTrigger(List<EvolutionDetail> details) {
        if (details == null || details.isEmpty()) {
            return null;
        }
        EvolutionDetail detail = details.stream()
                .filter(candidate -> Boolean.TRUE.equals(candidate.isDefault()))
                .findFirst()
                .orElse(details.getFirst());
        String trigger = detail.trigger() == null ? "" : detail.trigger().name();
        String description = switch (trigger) {
            case "level-up" -> describeLevelUp(detail);
            case "use-item" -> "Use " + humanize(detail.item());
            case "trade" -> detail.heldItem() == null ? "Trade" : "Trade holding " + humanize(detail.heldItem());
            default -> trigger.isEmpty() ? "Special condition" : capitalize(trigger.replace('-', ' '));
        };
        if (detail.timeOfDay() != null && !detail.timeOfDay().isBlank()) {
            description += " (" + detail.timeOfDay() + ")";
        }
        return description;
    }

    private static String describeLevelUp(EvolutionDetail detail) {
        if (detail.minLevel() != null) {
            return "Level " + detail.minLevel();
        }
        if (detail.knownMoveType() != null) {
            return "Level up knowing a " + humanize(detail.knownMoveType()) + "-type move";
        }
        if (detail.minHappiness() != null) {
            return "Level up with high friendship";
        }
        if (detail.knownMove() != null) {
            return "Level up knowing " + humanize(detail.knownMove());
        }
        if (detail.heldItem() != null) {
            return "Level up holding " + humanize(detail.heldItem());
        }
        if (detail.location() != null) {
            return "Level up at " + humanize(detail.location());
        }
        return "Level up";
    }

    private static EvolutionNode toEvolutionNode(ChainLink link) {
        int id = idFromUrl(link.species().url()).orElse(0);
        List<EvolutionNode> children = new ArrayList<>();
        if (link.evolvesTo() != null) {
            link.evolvesTo().forEach(child -> children.add(toEvolutionNode(child)));
        }
        return new EvolutionNode(id, link.species().name(), SPRITE_URL.formatted(id),
                describeTrigger(link.evolutionDetails()), children);
    }

    private static String sprite(Pokemon pokemon) {
        return pokemon.sprites() == null ? null : pokemon.sprites().frontDefault();
    }

    private static String artwork(Pokemon pokemon) {
        if (pokemon.sprites() != null && pokemon.sprites().other() != null
                && pokemon.sprites().other().officialArtwork() != null
                && pokemon.sprites().other().officialArtwork().frontDefault() != null) {
            return pokemon.sprites().other().officialArtwork().frontDefault();
        }
        return sprite(pokemon);
    }

    private static List<String> types(Pokemon pokemon) {
        if (pokemon.types() == null) {
            return List.of();
        }
        return pokemon.types().stream()
                .sorted(Comparator.comparingInt(TypeSlot::slot))
                .map(slot -> slot.type().name())
                .toList();
    }

    private static List<String> abilities(Pokemon pokemon) {
        if (pokemon.abilities() == null) {
            return List.of();
        }
        return pokemon.abilities().stream()
                .sorted(Comparator.comparingInt(AbilitySlot::slot))
                .map(slot -> slot.ability().name())
                .toList();
    }

    private static List<Stat> stats(Pokemon pokemon) {
        if (pokemon.stats() == null) {
            return List.of();
        }
        return pokemon.stats().stream()
                .map(entry -> new Stat(entry.stat().name(), entry.baseStat()))
                .toList();
    }

    private static boolean isEnglish(NamedResource language) {
        return language != null && ENGLISH.equals(language.name());
    }

    private static String humanize(NamedResource resource) {
        return resource == null ? "item" : resource.name().replace('-', ' ');
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
