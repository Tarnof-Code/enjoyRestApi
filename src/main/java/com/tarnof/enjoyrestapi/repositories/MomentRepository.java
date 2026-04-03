package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Integer> {

    long countBySejourId(int sejourId);

    @Query(
            "SELECT m FROM Moment m WHERE m.sejour.id = :sejourId "
                    + "ORDER BY COALESCE(m.ordre, m.id) ASC, m.id ASC")
    List<Moment> findBySejourIdOrderChronologique(@Param("sejourId") int sejourId);

    Optional<Moment> findByIdAndSejourId(int id, int sejourId);

    boolean existsBySejourIdAndNomIgnoreCase(int sejourId, String nom);

    boolean existsBySejourIdAndNomIgnoreCaseAndIdNot(int sejourId, String nom, int id);
}
