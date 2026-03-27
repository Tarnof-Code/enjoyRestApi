package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Lieu;
import com.tarnof.enjoyrestapi.entities.Sejour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LieuRepository extends JpaRepository<Lieu, Integer> {
    List<Lieu> findBySejour(Sejour sejour);

    List<Lieu> findBySejourId(int sejourId);

    boolean existsBySejourIdAndNomIgnoreCase(int sejourId, String nom);

    boolean existsBySejourIdAndNomIgnoreCaseAndIdNot(int sejourId, String nom, int id);
}
