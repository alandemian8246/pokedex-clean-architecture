package com.pokedex.web.common;

import com.pokedex.domain.model.PageResult;

import java.util.List;
import java.util.function.Function;

/**
 * Uniform envelope for every paginated endpoint.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <D, T> PageResponse<T> from(PageResult<D> result, Function<? super D, ? extends T> mapper) {
        return new PageResponse<>(
                result.items().stream().<T>map(mapper).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }
}
