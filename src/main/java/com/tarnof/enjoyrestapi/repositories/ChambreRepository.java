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
            "SELECT c FROM Chambre c WHERE c.sejour.id = :sejourId "
                    + "ORDER BY c.batiment ASC, c.etage ASC, c.couloir ASC, c.identifiant ASC")
    List<Chambre> findBySejourIdOrderAffichage(@Param("sejourId") int sejourId);

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
