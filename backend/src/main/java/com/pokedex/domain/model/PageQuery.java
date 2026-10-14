package com.pokedex.domain.model;

import com.pokedex.domain.validation.Validator;

public record PageQuery(int page, int size) {

    public static final int MAX_SIZE = 100;

    public PageQuery {
        new Validator()
                .check(page >= 0, "page", "must be greater than or equal to 0")
                .check(size >= 1 && size <= MAX_SIZE, "size", "must be between 1 and " + MAX_SIZE)
                .throwIfInvalid();
    }

    public int offset() {
        return page * size;
    }
}
