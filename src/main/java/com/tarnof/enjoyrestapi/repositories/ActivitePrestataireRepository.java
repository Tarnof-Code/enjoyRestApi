package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.ActivitePrestataire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActivitePrestataireRepository extends JpaRepository<ActivitePrestataire, Integer> {

    List<ActivitePrestataire> findBySejour_IdOrderByDateAscIdAsc(int sejourId);

    Optional<ActivitePrestataire> findByIdAndSejour_Id(int id, int sejourId);

    boolean existsByMoments_Id(int momentId);

    /**
     * Autre sortie du séjour, même jour, même moment, même groupe (hors {@code excludeActivitePrestataireId} en mise à jour).
     */
    @Query("SELECT COUNT(ap) FROM ActivitePrestataire ap JOIN ap.moments m JOIN ap.groupes g "
            + "WHERE ap.sejour.id = :sejourId AND ap.date = :date AND m.id = :momentId AND g.id = :groupeId "
            + "AND (:excludeActivitePrestataireId IS NULL OR ap.id <> :excludeActivitePrestataireId)")
    long countAutreSortieMemeDateMomentGroupe(
            @Param("sejourId") int sejourId,
            @Param("date") LocalDate date,
            @Param("momentId") int momentId,
            @Param("groupeId") int groupeId,
            @Param("excludeActivitePrestataireId") Integer excludeActivitePrestataireId);
}
