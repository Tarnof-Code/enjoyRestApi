package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "sejour_enfant")
public class SejourEnfant {
    @EmbeddedId
    private SejourEnfantId id;

    @MapsId("sejourId")
    @ManyToOne
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    @MapsId("enfantId")
    @ManyToOne
    @JoinColumn(name = "enfant_id", nullable = false)
    private Enfant enfant;

    public SejourEnfant() {
    }

    public SejourEnfant(Sejour sejour, Enfant enfant) {
        this.sejour = sejour;
        this.enfant = enfant;
        if (sejour != null && enfant != null) {
            this.id = new SejourEnfantId(sejour.getId(), enfant.getId());
        } else {
            this.id = new SejourEnfantId();
        }
    }

    public static SejourEnfantBuilder builder() {
        return new SejourEnfantBuilder();
    }

    public SejourEnfantId getId() {
        return id;
    }

    public void setId(SejourEnfantId id) {
        this.id = id;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    public Enfant getEnfant() {
        return enfant;
    }

    public void setEnfant(Enfant enfant) {
        this.enfant = enfant;
    }

    @Override
    public String toString() {
        return "SejourEnfant{" +
                "id=" + id +
                ", sejour=" + sejour +
                ", enfant=" + enfant +
                '}';
    }

    public static class SejourEnfantBuilder {
        private Sejour sejour;
        private Enfant enfant;

        public SejourEnfantBuilder sejour(Sejour sejour) {
            this.sejour = sejour;
            return this;
        }

        public SejourEnfantBuilder enfant(Enfant enfant) {
            this.enfant = enfant;
            return this;
        }

        public SejourEnfant build() {
            return new SejourEnfant(sejour, enfant);
        }
    }
}
