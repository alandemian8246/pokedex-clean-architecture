package com.pokedex.domain.model;

import com.pokedex.domain.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PageQueryTest {

    @Test
    void computesOffset() {
        assertThat(new PageQuery(3, 20).offset()).isEqualTo(60);
    }

    @ParameterizedTest
    @CsvSource({"-1, 20, page", "0, 0, size", "0, 101, size"})
    void rejectsOutOfRangeValues(int page, int size, String field) {
        assertThatThrownBy(() -> new PageQuery(page, size))
                .isInstanceOf(ValidationException.class)
                .satisfies(ex -> assertThat(((ValidationException) ex).errors()).containsKey(field));
    }

    @Test
    void pageResultComputesTotalPagesAndMapsItems() {
        PageResult<Integer> result = new PageResult<>(List.of(1, 2), 0, 2, 5);

        assertThat(result.totalPages()).isEqualTo(3);
        assertThat(result.map(i -> "#" + i).items()).containsExactly("#1", "#2");
    }
}
