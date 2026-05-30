package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Chambre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChambreRepository extends JpaRepository<Chambre, Integer> {

    Optional<Chambre> findByIdAndSejourId(int id, int sejourId);

    @Query(
            "SELECT DISTINCT c FROM Chambre c "
                    + "LEFT JOIN FETCH c.occupants o "
                    + "LEFT JOIN FETCH o.enfant "
                    + "LEFT JOIN FETCH o.utilisateur "
                    + "WHERE c.sejour.id = :sejourId "
                    + "ORDER BY c.batiment ASC, c.etage ASC, c.couloir ASC, c.identifiant ASC")
    List<Chambre> findBySejourIdOrderAffichageWithOccupants(@Param("sejourId") int sejourId);

    @Query(
            "SELECT DISTINCT c FROM Chambre c "
                    + "LEFT JOIN FETCH c.occupants o "
                    + "LEFT JOIN FETCH o.enfant "
                    + "LEFT JOIN FETCH o.utilisateur "
                    + "WHERE c.id = :chambreId AND c.sejour.id = :sejourId")
    Optional<Chambre> findByIdAndSejourIdWithOccupants(
            @Param("chambreId") int chambreId, @Param("sejourId") int sejourId);

    /** Deuxième requête dédiée : Hibernate interdit deux JOIN FETCH sur des List (bags) dans la même requête. */
    @Query("SELECT DISTINCT c FROM Chambre c LEFT JOIN FETCH c.referents WHERE c.id IN :chambreIds")
    List<Chambre> fetchReferentsByChambreIds(@Param("chambreIds") List<Integer> chambreIds);

    @Query(
            "SELECT COUNT(c) > 0 FROM Chambre c WHERE c.sejour.id = :sejourId "
                    + "AND LOWER(c.identifiant) = LOWER(:identifiant)")
    boolean existsBySejourIdAndIdentifiantIgnoreCase(
            @Param("sejourId") int sejourId, @Param("identifiant") String identifiant);

    @Query(
            "SELECT COUNT(c) > 0 FROM Chambre c WHERE c.sejour.id = :sejourId "
                    + "AND LOWER(c.identifiant) = LOWER(:identifiant) AND c.id <> :excludeId")
    boolean existsBySejourIdAndIdentifiantIgnoreCaseAndIdNot(
            @Param("sejourId") int sejourId,
            @Param("identifiant") String identifiant,
            @Param("excludeId") int excludeId);
}
