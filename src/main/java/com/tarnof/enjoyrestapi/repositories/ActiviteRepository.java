package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Activite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Integer> {

    List<Activite> findBySejourIdOrderByDateAscIdAsc(int sejourId);

    Optional<Activite> findByIdAndSejourId(int id, int sejourId);

    long countBySejour_IdAndLieu_IdAndDate(int sejourId, int lieuId, LocalDate date);

    long countBySejour_IdAndLieu_IdAndDateAndIdNot(int sejourId, int lieuId, LocalDate date, int activiteId);
}
