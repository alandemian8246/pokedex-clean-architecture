package com.pokedex.infrastructure.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataLocalPokemonRepository extends JpaRepository<LocalPokemonEntity, Integer> {

    @Query("""
            select p from LocalPokemonEntity p
            where lower(p.name) like :pattern escape '\\'
               or lower(p.localizedName) like :pattern escape '\\'
            """)
    Page<LocalPokemonEntity> search(@Param("pattern") String pattern, Pageable pageable);
}
