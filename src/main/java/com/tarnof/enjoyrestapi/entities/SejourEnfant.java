package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sejour_enfant")
@Data
@NoArgsConstructor
@AllArgsConstructor
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
    
    @Builder
    public SejourEnfant(Sejour sejour, Enfant enfant) {
        this.sejour = sejour;
        this.enfant = enfant;
        // Initialiser l'ID avec les valeurs des entités (JPA remplira automatiquement avec @MapsId lors de la persistance)
        if (sejour != null && enfant != null) {
            this.id = new SejourEnfantId(sejour.getId(), enfant.getId());
        } else {
            this.id = new SejourEnfantId();
        }
    }
}
