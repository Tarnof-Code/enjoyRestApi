package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.Moment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MomentRepository extends JpaRepository<Moment, Integer> {

    long countBySejourId(int sejourId);

    List<Moment> findBySejourIdOrderByNomAscIdAsc(int sejourId);

    Optional<Moment> findByIdAndSejourId(int id, int sejourId);

    boolean existsBySejourIdAndNomIgnoreCase(int sejourId, String nom);

    boolean existsBySejourIdAndNomIgnoreCaseAndIdNot(int sejourId, String nom, int id);
}
