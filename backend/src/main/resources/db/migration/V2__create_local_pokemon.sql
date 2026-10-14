CREATE TABLE local_pokemon (
    id             INTEGER                  NOT NULL PRIMARY KEY,
    name           VARCHAR(100)             NOT NULL,
    category       VARCHAR(100),
    height         INTEGER                  NOT NULL,
    weight         INTEGER                  NOT NULL,
    sprite_url     VARCHAR(500),
    artwork_url    VARCHAR(500),
    description    VARCHAR(1000),
    localized_name VARCHAR(100),
    region         VARCHAR(50),
    notes          VARCHAR(500),
    synced_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    version        BIGINT                   NOT NULL DEFAULT 0,
    CONSTRAINT uk_local_pokemon_name UNIQUE (name),
    CONSTRAINT ck_local_pokemon_id CHECK (id > 0)
);

CREATE TABLE local_pokemon_type (
    pokemon_id INTEGER     NOT NULL REFERENCES local_pokemon (id) ON DELETE CASCADE,
    position   INTEGER     NOT NULL,
    type_name  VARCHAR(30) NOT NULL,
    PRIMARY KEY (pokemon_id, position)
);

CREATE TABLE local_pokemon_ability (
    pokemon_id   INTEGER     NOT NULL REFERENCES local_pokemon (id) ON DELETE CASCADE,
    position     INTEGER     NOT NULL,
    ability_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (pokemon_id, position)
);

CREATE TABLE local_pokemon_stat (
    pokemon_id INTEGER     NOT NULL REFERENCES local_pokemon (id) ON DELETE CASCADE,
    position   INTEGER     NOT NULL,
    stat_name  VARCHAR(30) NOT NULL,
    base_value INTEGER     NOT NULL,
    PRIMARY KEY (pokemon_id, position)
);

CREATE TABLE local_pokemon_tag (
    pokemon_id INTEGER     NOT NULL REFERENCES local_pokemon (id) ON DELETE CASCADE,
    position   INTEGER     NOT NULL,
    tag        VARCHAR(30) NOT NULL,
    PRIMARY KEY (pokemon_id, position)
);

CREATE INDEX ix_local_pokemon_tag_tag ON local_pokemon_tag (tag);
