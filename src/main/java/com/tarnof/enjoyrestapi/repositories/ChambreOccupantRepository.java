package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.ChambreOccupant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChambreOccupantRepository extends JpaRepository<ChambreOccupant, Integer> {

    long countByChambreId(int chambreId);

    List<ChambreOccupant> findByChambreId(int chambreId);

    @Query(
            "SELECT o FROM ChambreOccupant o "
                    + "WHERE o.enfant.id = :enfantId AND o.chambre.sejour.id = :sejourId")
    Optional<ChambreOccupant> findByEnfantIdAndSejourId(
            @Param("enfantId") int enfantId, @Param("sejourId") int sejourId);

    @Query(
            "SELECT o FROM ChambreOccupant o "
                    + "WHERE o.utilisateur.id = :utilisateurId AND o.chambre.sejour.id = :sejourId")
    Optional<ChambreOccupant> findByUtilisateurIdAndSejourId(
            @Param("utilisateurId") int utilisateurId, @Param("sejourId") int sejourId);

    @Query(
            "SELECT COUNT(o) > 0 FROM ChambreOccupant o "
                    + "WHERE o.chambre.id = :chambreId AND o.numeroLit = :numeroLit "
                    + "AND (:excludeOccupantId IS NULL OR o.id <> :excludeOccupantId)")
    boolean existsByChambreIdAndNumeroLitExcluding(
            @Param("chambreId") int chambreId,
            @Param("numeroLit") int numeroLit,
            @Param("excludeOccupantId") Integer excludeOccupantId);
}
