package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Activite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Integer> {

    List<Activite> findBySejourIdOrderByDateAscIdAsc(int sejourId);

    Optional<Activite> findByIdAndSejourId(int id, int sejourId);
}
