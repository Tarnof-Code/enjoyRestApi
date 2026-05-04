package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.ReferenceAlimentaire;
import com.tarnof.enjoyrestapi.enums.TypeReferenceAlimentaire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReferenceAlimentaireRepository extends JpaRepository<ReferenceAlimentaire, Integer> {

    /** Même ordre logique que l’ancien tri en mémoire : type puis ordre puis id. */
    List<ReferenceAlimentaire> findAllByOrderByTypeAscOrdreAscIdAsc();

    List<ReferenceAlimentaire> findByTypeOrderByOrdreAscIdAsc(TypeReferenceAlimentaire type);

    boolean existsByTypeAndLibelleIgnoreCase(TypeReferenceAlimentaire type, String libelle);

    Optional<ReferenceAlimentaire> findByTypeAndLibelleIgnoreCase(TypeReferenceAlimentaire type, String libelle);

    @Query("SELECT COUNT(d) FROM DossierEnfant d JOIN d.allergenes r WHERE r.id = :refId")
    long countUsageAsAllergene(@Param("refId") int refId);

    @Query("SELECT COUNT(d) FROM DossierEnfant d JOIN d.regimesEtPreferences r WHERE r.id = :refId")
    long countUsageAsRegime(@Param("refId") int refId);

    @Query("SELECT COUNT(DISTINCT m.id) FROM MenuRepas m JOIN m.allergenes r WHERE r.id = :refId")
    long countUsageMenuAsAllergene(@Param("refId") int refId);

    @Query("SELECT COUNT(DISTINCT m.id) FROM MenuRepas m JOIN m.regimesEtPreferences r WHERE r.id = :refId")
    long countUsageMenuAsRegime(@Param("refId") int refId);
}
