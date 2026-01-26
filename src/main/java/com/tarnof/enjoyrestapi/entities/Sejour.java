package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sejour {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nom;
    private String description;
    private Date dateDebut;
    private Date dateFin;
    private String lieuDuSejour;
    @ManyToOne
    private Utilisateur directeur;
    @OneToMany(mappedBy = "sejour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SejourEquipe> equipeRoles;
    @OneToMany(mappedBy = "sejour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SejourEnfant> enfants;
}
