package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.PlanningGrille;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanningGrilleRepository extends JpaRepository<PlanningGrille, Integer> {

    @EntityGraph(attributePaths = "sejour")
    List<PlanningGrille> findBySejour_IdOrderByMiseAJourDesc(int sejourId);

    Optional<PlanningGrille> findByIdAndSejour_Id(int id, int sejourId);
}
