package com.tarnof.enjoyrestapi.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tarnof.enjoyrestapi.entities.DossierEnfant;

public interface DossierEnfantRepository extends JpaRepository<DossierEnfant, Integer> {
    Optional<DossierEnfant> findByEnfantId(int enfantId);
}
