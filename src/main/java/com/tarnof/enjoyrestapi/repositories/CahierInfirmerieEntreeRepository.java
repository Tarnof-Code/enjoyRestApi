package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.CahierInfirmerieEntree;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CahierInfirmerieEntreeRepository extends JpaRepository<CahierInfirmerieEntree, Integer> {

    @Query(
            "SELECT c FROM CahierInfirmerieEntree c JOIN FETCH c.enfant e LEFT JOIN FETCH c.createur "
                    + "JOIN FETCH c.soigneur "
                    + "WHERE c.sejour.id = :sejourId ORDER BY c.dateHeure DESC, c.id DESC")
    List<CahierInfirmerieEntree> findBySejourIdWithEnfantOrderByDateHeureDesc(@Param("sejourId") int sejourId);

    @Query(
            "SELECT c FROM CahierInfirmerieEntree c JOIN FETCH c.enfant LEFT JOIN FETCH c.createur "
                    + "JOIN FETCH c.soigneur "
                    + "WHERE c.id = :id AND c.sejour.id = :sejourId")
    Optional<CahierInfirmerieEntree> findByIdAndSejourIdWithEnfantAndCreateur(
            @Param("id") int id, @Param("sejourId") int sejourId);
}
