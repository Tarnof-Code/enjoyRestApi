package com.tarnof.enjoyrestapi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;

public interface SejourRepository extends JpaRepository<Sejour, Integer> {
    List<Sejour> findByDirecteur(Utilisateur directeur);
}
