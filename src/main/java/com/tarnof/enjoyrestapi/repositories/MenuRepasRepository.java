package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.MenuRepas;
import com.tarnof.enjoyrestapi.enums.TypeRepas;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MenuRepasRepository extends JpaRepository<MenuRepas, Integer> {

    @EntityGraph(attributePaths = {"allergenes", "regimesEtPreferences"})
    Optional<MenuRepas> findBySejour_IdAndDateRepasAndTypeRepas(int sejourId, LocalDate dateRepas, TypeRepas typeRepas);

    @EntityGraph(attributePaths = {"allergenes", "regimesEtPreferences"})
    List<MenuRepas> findBySejour_IdAndDateRepasOrderByTypeRepasAsc(int sejourId, LocalDate dateRepas);

    @EntityGraph(attributePaths = {"allergenes", "regimesEtPreferences"})
    List<MenuRepas> findBySejour_IdAndDateRepasBetweenOrderByDateRepasAscTypeRepasAsc(
            int sejourId, LocalDate dateDebutInclusive, LocalDate dateFinInclusive);

    @EntityGraph(attributePaths = {"allergenes", "regimesEtPreferences"})
    @Query("SELECT m FROM MenuRepas m WHERE m.id = :id")
    Optional<MenuRepas> findByIdFetchingReferences(@Param("id") Integer id);
}
