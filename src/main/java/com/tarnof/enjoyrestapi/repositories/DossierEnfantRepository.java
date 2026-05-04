package com.tarnof.enjoyrestapi.repositories;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tarnof.enjoyrestapi.entities.DossierEnfant;

public interface DossierEnfantRepository extends JpaRepository<DossierEnfant, Integer> {
    Optional<DossierEnfant> findByEnfantId(int enfantId);

    @EntityGraph(attributePaths = {"allergenes", "regimesEtPreferences"})
    @Query("SELECT d FROM DossierEnfant d WHERE d.enfant.id = :enfantId")
    Optional<DossierEnfant> findByEnfantIdFetchingReferences(@Param("enfantId") int enfantId);

    @EntityGraph(attributePaths = {"allergenes", "regimesEtPreferences"})
    @Query("SELECT d FROM DossierEnfant d WHERE d.enfant.id IN :enfantIds")
    List<DossierEnfant> findByEnfantIdInFetchingReferences(@Param("enfantIds") Collection<Integer> enfantIds);
}
