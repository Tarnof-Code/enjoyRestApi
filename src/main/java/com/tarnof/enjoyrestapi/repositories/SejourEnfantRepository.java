package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.SejourEnfant;
import com.tarnof.enjoyrestapi.entities.SejourEnfantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SejourEnfantRepository extends JpaRepository<SejourEnfant, SejourEnfantId> {
    /**
     * Compte le nombre de séjours auxquels un enfant est inscrit
     * @param enfantId L'ID de l'enfant
     * @return Le nombre de séjours
     */
    @Query("SELECT COUNT(se) FROM SejourEnfant se WHERE se.enfant.id = :enfantId")
    long countByEnfantId(@Param("enfantId") int enfantId);
}
