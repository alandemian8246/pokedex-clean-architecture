package com.pokedex.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class StatEmbeddable {

    @Column(name = "stat_name", nullable = false, length = 30)
    private String name;

    @Column(name = "base_value", nullable = false)
    private int baseValue;

    protected StatEmbeddable() {
        // required by JPA
    }

    StatEmbeddable(String name, int baseValue) {
        this.name = name;
        this.baseValue = baseValue;
    }

    public String getName() { return name; }
    public int getBaseValue() { return baseValue; }
}
