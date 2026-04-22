package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Activite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Integer> {

    List<Activite> findBySejourIdOrderByDateAscIdAsc(int sejourId);

    Optional<Activite> findByIdAndSejourId(int id, int sejourId);

    long countBySejour_IdAndLieu_IdAndDateAndMoment_Id(
            int sejourId, int lieuId, LocalDate date, int momentId);

    long countBySejour_IdAndLieu_IdAndDateAndMoment_IdAndIdNot(
            int sejourId, int lieuId, LocalDate date, int momentId, int activiteId);

    boolean existsByMomentId(int momentId);

    long countByTypeActivite_Id(int typeActiviteId);

    /**
     * Compte les activités du séjour, ce jour, ce moment, assignées à l'utilisateur.
     * Si {@code excludeActiviteId} n'est pas null, cette activité est exclue (mise à jour d'une fiche existante).
     */
    @Query("SELECT COUNT(a) FROM Activite a JOIN a.membres m "
            + "WHERE a.sejour.id = :sejourId AND a.date = :date AND a.moment.id = :momentId "
            + "AND m.id = :utilisateurId AND (:excludeActiviteId IS NULL OR a.id <> :excludeActiviteId)")
    long countActivitesAvecMembreMemeCreneau(
            @Param("sejourId") int sejourId,
            @Param("date") LocalDate date,
            @Param("momentId") int momentId,
            @Param("utilisateurId") int utilisateurId,
            @Param("excludeActiviteId") Integer excludeActiviteId);
}
