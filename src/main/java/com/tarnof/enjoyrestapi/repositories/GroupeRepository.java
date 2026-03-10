package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Groupe;
import com.tarnof.enjoyrestapi.entities.Sejour;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GroupeRepository extends JpaRepository<Groupe, Integer> {
    List<Groupe> findBySejour(Sejour sejour);
    List<Groupe> findBySejourId(int sejourId);
}