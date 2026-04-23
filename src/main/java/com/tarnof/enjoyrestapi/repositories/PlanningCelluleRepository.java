package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.PlanningCellule;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlanningCelluleRepository extends JpaRepository<PlanningCellule, Integer> {

    @EntityGraph(attributePaths = {"animateursAssignes", "horaire"})
    List<PlanningCellule> findByLigne_IdIn(Collection<Integer> ligneIds);

    Optional<PlanningCellule> findByLigne_IdAndJour(int ligneId, LocalDate jour);
}
