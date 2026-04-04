package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.TypeActivite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeActiviteRepository extends JpaRepository<TypeActivite, Integer> {

    List<TypeActivite> findBySejourIdOrderByLibelleAsc(int sejourId);

    Optional<TypeActivite> findByIdAndSejourId(int id, int sejourId);

    Optional<TypeActivite> findBySejourIdAndLibelleIgnoreCase(int sejourId, String libelle);

    boolean existsBySejourIdAndLibelleIgnoreCase(int sejourId, String libelle);

    boolean existsBySejourIdAndLibelleIgnoreCaseAndIdNot(int sejourId, String libelle, int id);
}
