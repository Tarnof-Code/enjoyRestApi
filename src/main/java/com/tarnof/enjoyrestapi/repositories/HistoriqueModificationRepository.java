package com.tarnof.enjoyrestapi.repositories;

import com.tarnof.enjoyrestapi.entities.HistoriqueModification;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationActivite;
import com.tarnof.enjoyrestapi.entities.HistoriqueModificationPlanningCellule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HistoriqueModificationRepository extends JpaRepository<HistoriqueModification, Integer> {

    @Query(
            "SELECT h FROM HistoriqueModificationPlanningCellule h "
                    + "JOIN FETCH h.modificateur "
                    + "WHERE h.planningLigneId = :ligneId AND h.planningJour = :jour "
                    + "ORDER BY h.dateModification DESC")
    List<HistoriqueModificationPlanningCellule> findPlanningByLigneIdAndJour(
            @Param("ligneId") int ligneId, @Param("jour") LocalDate jour);

    @Query(
            "SELECT h FROM HistoriqueModificationPlanningCellule h "
                    + "JOIN FETCH h.modificateur "
                    + "WHERE h.planningLigneId = :ligneId "
                    + "ORDER BY h.dateModification DESC")
    List<HistoriqueModificationPlanningCellule> findPlanningByLigneId(@Param("ligneId") int ligneId);

    @Query(
            "SELECT h FROM HistoriqueModificationActivite h "
                    + "JOIN FETCH h.modificateur "
                    + "WHERE h.activiteId = :activiteId "
                    + "ORDER BY h.dateModification DESC")
    List<HistoriqueModificationActivite> findActiviteByActiviteId(@Param("activiteId") int activiteId);
}
