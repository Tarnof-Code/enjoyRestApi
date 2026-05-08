package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Sejour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupeRepository extends JpaRepository<Groupe, Integer> {
    List<Groupe> findBySejour(Sejour sejour);
    List<Groupe> findBySejourId(int sejourId);

    @Query("SELECT DISTINCT g FROM Groupe g LEFT JOIN FETCH g.enfants WHERE g.sejour.id = :sejourId")
    List<Groupe> findBySejourIdFetchingEnfants(@Param("sejourId") int sejourId);

    Optional<Groupe> findByIdAndSejourId(int id, int sejourId);
}