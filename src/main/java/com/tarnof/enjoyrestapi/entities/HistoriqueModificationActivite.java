package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ACTIVITE")
public class HistoriqueModificationActivite extends HistoriqueModification {

    @Column(name = "activite_id")
    private Integer activiteId;

    public HistoriqueModificationActivite() {}

    public Integer getActiviteId() {
        return activiteId;
    }

    public void setActiviteId(Integer activiteId) {
        this.activiteId = activiteId;
    }
}
