package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.RoleSejour;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sejour_equipe")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SejourEquipe {
    @EmbeddedId
    private SejourEquipeId id;
    
    @MapsId("sejourId")
    @ManyToOne
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;
    
    @MapsId("utilisateurId")
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleSejour roleSejour;
    
    @Builder
    public SejourEquipe(Sejour sejour, Utilisateur utilisateur, RoleSejour roleSejour) {
        this.id = new SejourEquipeId(); 
        this.sejour = sejour;
        this.utilisateur = utilisateur;
        this.roleSejour = roleSejour;
    }
}

