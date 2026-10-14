-- Demonstration data: the Kanto starters, Pikachu and Eevee, replicated from PokeAPI
-- and enriched with proprietary fields (Japanese name, region, internal tags, notes).

INSERT INTO local_pokemon (id, name, category, height, weight, sprite_url, artwork_url, description,
                           localized_name, region, notes, synced_at, updated_at, version)
VALUES
    (1, 'bulbasaur', 'Seed Pokémon', 7, 69,
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/1.png',
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/1.png',
     'A strange seed was planted on its back at birth. The plant sprouts and grows with this POKéMON.',
     'フシギダネ', 'Kanto', 'Recommended starter for new trainers.',
     TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', 0),
    (4, 'charmander', 'Lizard Pokémon', 6, 85,
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/4.png',
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/4.png',
     'Obviously prefers hot places. When it rains, steam is said to spout from the tip of its tail.',
     'ヒトカゲ', 'Kanto', NULL,
     TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', 0),
    (7, 'squirtle', 'Tiny Turtle Pokémon', 5, 90,
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/7.png',
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/7.png',
     'After birth, its back swells and hardens into a shell. Powerfully sprays foam from its mouth.',
     'ゼニガメ', 'Kanto', NULL,
     TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', 0),
    (25, 'pikachu', 'Mouse Pokémon', 4, 60,
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/25.png',
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png',
     'When several of these POKéMON gather, their electricity could build and cause lightning storms.',
     'ピカチュウ', 'Viridian Forest, Kanto', 'Mascot. Keep away from water-type exhibits.',
     TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', 0),
    (133, 'eevee', 'Evolution Pokémon', 3, 65,
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/133.png',
     'https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/133.png',
     'Its genetic code is irregular. It may mutate if it is exposed to radiation from element STONEs.',
     'イーブイ', 'Celadon City, Kanto', 'Eight possible evolutions; track the chosen branch.',
     TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', TIMESTAMP WITH TIME ZONE '2026-01-01 00:00:00+00', 0);

INSERT INTO local_pokemon_type (pokemon_id, position, type_name)
VALUES (1, 0, 'grass'), (1, 1, 'poison'),
       (4, 0, 'fire'),
       (7, 0, 'water'),
       (25, 0, 'electric'),
       (133, 0, 'normal');

INSERT INTO local_pokemon_ability (pokemon_id, position, ability_name)
VALUES (1, 0, 'overgrow'), (1, 1, 'chlorophyll'),
       (4, 0, 'blaze'), (4, 1, 'solar-power'),
       (7, 0, 'torrent'), (7, 1, 'rain-dish'),
       (25, 0, 'static'), (25, 1, 'lightning-rod'),
       (133, 0, 'run-away'), (133, 1, 'adaptability'), (133, 2, 'anticipation');

INSERT INTO local_pokemon_stat (pokemon_id, position, stat_name, base_value)
VALUES (1, 0, 'hp', 45), (1, 1, 'attack', 49), (1, 2, 'defense', 49),
       (1, 3, 'special-attack', 65), (1, 4, 'special-defense', 65), (1, 5, 'speed', 45),
       (4, 0, 'hp', 39), (4, 1, 'attack', 52), (4, 2, 'defense', 43),
       (4, 3, 'special-attack', 60), (4, 4, 'special-defense', 50), (4, 5, 'speed', 65),
       (7, 0, 'hp', 44), (7, 1, 'attack', 48), (7, 2, 'defense', 65),
       (7, 3, 'special-attack', 50), (7, 4, 'special-defense', 64), (7, 5, 'speed', 43),
       (25, 0, 'hp', 35), (25, 1, 'attack', 55), (25, 2, 'defense', 40),
       (25, 3, 'special-attack', 50), (25, 4, 'special-defense', 50), (25, 5, 'speed', 90),
       (133, 0, 'hp', 55), (133, 1, 'attack', 55), (133, 2, 'defense', 50),
       (133, 3, 'special-attack', 45), (133, 4, 'special-defense', 65), (133, 5, 'speed', 55);

INSERT INTO local_pokemon_tag (pokemon_id, position, tag)
VALUES (1, 0, 'starter'), (1, 1, 'gen-1'),
       (4, 0, 'starter'), (4, 1, 'gen-1'),
       (7, 0, 'starter'), (7, 1, 'gen-1'),
       (25, 0, 'mascot'), (25, 1, 'gen-1'), (25, 2, 'fan-favorite'),
       (133, 0, 'gen-1'), (133, 1, 'branching-evolution');
