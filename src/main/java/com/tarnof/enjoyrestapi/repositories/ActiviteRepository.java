package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Activite;
import com.tarnof.enjoyrestapi.entities.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ActiviteRepository extends JpaRepository<Activite, Integer> {

    List<Activite> findBySejourIdOrderByDateAscIdAsc(int sejourId);

    Optional<Activite> findByIdAndSejourId(int id, int sejourId);

    /**
     * Compte les activités sur ce lieu, ce jour, pour l'un des moments de {@code momentIds}
     * (moment visé, ancêtres et descendants — chevauchement hiérarchique).
     * Si {@code excludeActiviteId} n'est pas null, cette activité est exclue (mise à jour).
     */
    @Query("SELECT COUNT(a) FROM Activite a "
            + "WHERE a.sejour.id = :sejourId AND a.lieu.id = :lieuId AND a.date = :date "
            + "AND a.moment.id IN :momentIds "
            + "AND (:excludeActiviteId IS NULL OR a.id <> :excludeActiviteId)")
    long countBySejour_IdAndLieu_IdAndDateAndMoment_IdIn(
            @Param("sejourId") int sejourId,
            @Param("lieuId") int lieuId,
            @Param("date") LocalDate date,
            @Param("momentIds") Collection<Integer> momentIds,
            @Param("excludeActiviteId") Integer excludeActiviteId);

    boolean existsByMomentId(int momentId);

    long countByTypeActivite_Id(int typeActiviteId);

    /**
     * Moments déjà encadrés par l'utilisateur ce jour-là parmi l'ensemble {@code momentIds} (le
     * moment visé, ses ancêtres et ses descendants), pour détecter un chevauchement de hiérarchie.
     * Si {@code excludeActiviteId} n'est pas null, cette activité est exclue (mise à jour d'une fiche existante).
     */
    @Query("SELECT DISTINCT a.moment FROM Activite a JOIN a.membres m "
            + "WHERE a.sejour.id = :sejourId AND a.date = :date AND a.moment.id IN :momentIds "
            + "AND m.id = :utilisateurId AND (:excludeActiviteId IS NULL OR a.id <> :excludeActiviteId)")
    List<Moment> findMomentsEnConflitPourMembre(
            @Param("sejourId") int sejourId,
            @Param("date") LocalDate date,
            @Param("momentIds") Collection<Integer> momentIds,
            @Param("utilisateurId") int utilisateurId,
            @Param("excludeActiviteId") Integer excludeActiviteId);

    @Query("SELECT COUNT(a) FROM Activite a JOIN a.groupes g WHERE g.id = :groupeId")
    long countByGroupeId(@Param("groupeId") int groupeId);
}
