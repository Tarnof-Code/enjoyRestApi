package com.tarnof.enjoyrestapi.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;

public interface SejourRepository extends JpaRepository<Sejour, Integer> {
    List<Sejour> findByDirecteur(Utilisateur directeur);
    
    @Query("SELECT DISTINCT s FROM Sejour s LEFT JOIN s.equipeRoles se " +
           "WHERE s.directeur = :utilisateur OR se.utilisateur = :utilisateur")
    List<Sejour> findSejoursByUtilisateur(@Param("utilisateur") Utilisateur utilisateur);
}
