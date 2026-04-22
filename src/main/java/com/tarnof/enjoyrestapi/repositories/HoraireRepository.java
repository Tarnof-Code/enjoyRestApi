package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Horaire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoraireRepository extends JpaRepository<Horaire, Integer> {

    List<Horaire> findBySejourIdOrderByIdAsc(int sejourId);

    Optional<Horaire> findByIdAndSejourId(int id, int sejourId);

    boolean existsBySejourIdAndLibelleIgnoreCase(int sejourId, String libelle);

    boolean existsBySejourIdAndLibelleIgnoreCaseAndIdNot(int sejourId, String libelle, int id);
}
