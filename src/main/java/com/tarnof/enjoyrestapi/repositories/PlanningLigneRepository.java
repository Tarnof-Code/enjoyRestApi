package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.PlanningLigne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanningLigneRepository extends JpaRepository<PlanningLigne, Integer> {

    List<PlanningLigne> findByGrille_Id(int grilleId);

    Optional<PlanningLigne> findByIdAndGrille_Id(int id, int grilleId);
}
