package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.time.LocalDate;

@Entity
@DiscriminatorValue("PLANNING_CELLULE")
public class HistoriqueModificationPlanningCellule extends HistoriqueModification {

    @Column(name = "planning_ligne_id")
    private Integer planningLigneId;

    @Column(name = "planning_jour")
    private LocalDate planningJour;

    @Column(name = "planning_cellule_id")
    private Integer planningCelluleId;

    public HistoriqueModificationPlanningCellule() {}

    public Integer getPlanningLigneId() {
        return planningLigneId;
    }

    public void setPlanningLigneId(Integer planningLigneId) {
        this.planningLigneId = planningLigneId;
    }

    public LocalDate getPlanningJour() {
        return planningJour;
    }

    public void setPlanningJour(LocalDate planningJour) {
        this.planningJour = planningJour;
    }

    public Integer getPlanningCelluleId() {
        return planningCelluleId;
    }

    public void setPlanningCelluleId(Integer planningCelluleId) {
        this.planningCelluleId = planningCelluleId;
    }
}
