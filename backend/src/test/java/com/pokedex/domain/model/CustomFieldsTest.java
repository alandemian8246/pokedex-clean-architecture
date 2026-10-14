package com.pokedex.domain.model;

import com.pokedex.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomFieldsTest {

    @Test
    void normalizesBlankValuesToNullAndTrimsText() {
        CustomFields fields = new CustomFields("  Fushigidane  ", "   ", null, "");

        assertThat(fields.localizedName()).isEqualTo("Fushigidane");
        assertThat(fields.region()).isNull();
        assertThat(fields.notes()).isNull();
        assertThat(fields.tags()).isEmpty();
    }

    @Test
    void normalizesTagsToLowercaseRemovesDuplicatesAndKeepsOrder() {
        Set<String> raw = new LinkedHashSet<>(List.of("Starter", " gen-1 ", "starter", " "));

        CustomFields fields = new CustomFields(null, null, raw, null);

        assertThat(fields.tags()).containsExactly("starter", "gen-1");
    }

    @Test
    void rejectsTooLongLocalizedNameAndRegion() {
        assertThatThrownBy(() -> new CustomFields("x".repeat(101), "y".repeat(51), Set.of(), null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).errors())
                        .containsKeys("localizedName", "region"));
    }

    @Test
    void rejectsInvalidTagFormat() {
        assertThatThrownBy(() -> new CustomFields(null, null, Set.of("not valid!"), null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).errors()).containsKey("tags"));
    }

    @Test
    void rejectsMoreThanTenTags() {
        Set<String> tags = IntStream.rangeClosed(1, 11).mapToObj(i -> "tag" + i).collect(Collectors.toSet());

        assertThatThrownBy(() -> new CustomFields(null, null, tags, null))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).errors()).containsKey("tags"));
    }

    @Test
    void rejectsTooLongNotes() {
        assertThatThrownBy(() -> new CustomFields(null, null, Set.of(), "n".repeat(501)))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).errors()).containsKey("notes"));
    }

    @Test
    void acceptsValuesAtTheLimits() {
        CustomFields fields = new CustomFields("x".repeat(100), "y".repeat(50), Set.of("a".repeat(30)),
                "n".repeat(500));

        assertThat(fields.localizedName()).hasSize(100);
    }

    @Test
    void emptyHasNoValues() {
        assertThat(CustomFields.empty()).isEqualTo(new CustomFields(null, null, Set.of(), null));
    }
}
