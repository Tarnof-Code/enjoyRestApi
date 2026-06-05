package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ACTIVITE_PRESTATAIRE")
public class HistoriqueModificationActivitePrestataire extends HistoriqueModification {

    @Column(name = "activite_prestataire_id")
    private Integer activitePrestataireId;

    public HistoriqueModificationActivitePrestataire() {}

    public Integer getActivitePrestataireId() {
        return activitePrestataireId;
    }

    public void setActivitePrestataireId(Integer activitePrestataireId) {
        this.activitePrestataireId = activitePrestataireId;
    }
}
