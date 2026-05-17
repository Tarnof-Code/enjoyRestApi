package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CAHIER_INFIRMERIE")
public class HistoriqueModificationCahierInfirmerie extends HistoriqueModification {

    @Column(name = "cahier_infirmerie_entree_id")
    private Integer cahierInfirmerieEntreeId;

    public HistoriqueModificationCahierInfirmerie() {}

    public Integer getCahierInfirmerieEntreeId() {
        return cahierInfirmerieEntreeId;
    }

    public void setCahierInfirmerieEntreeId(Integer cahierInfirmerieEntreeId) {
        this.cahierInfirmerieEntreeId = cahierInfirmerieEntreeId;
    }
}
