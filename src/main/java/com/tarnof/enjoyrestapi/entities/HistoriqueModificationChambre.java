package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CHAMBRE")
public class HistoriqueModificationChambre extends HistoriqueModification {

    @Column(name = "chambre_id")
    private Integer chambreId;

    public HistoriqueModificationChambre() {}

    public Integer getChambreId() {
        return chambreId;
    }

    public void setChambreId(Integer chambreId) {
        this.chambreId = chambreId;
    }
}
