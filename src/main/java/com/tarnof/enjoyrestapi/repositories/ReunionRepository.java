package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Reunion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReunionRepository extends JpaRepository<Reunion, Integer> {

    List<Reunion> findBySejour_IdOrderByDateReunionAscIdAsc(int sejourId);

    Optional<Reunion> findByIdAndSejour_Id(int id, int sejourId);
}
