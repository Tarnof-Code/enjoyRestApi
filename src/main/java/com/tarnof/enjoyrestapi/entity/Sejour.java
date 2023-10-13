package com.tarnof.enjoyrestapi.entity;

import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

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
    @ManyToMany
    @JoinTable(
            name = "sejour_animateurs",
            joinColumns = @JoinColumn(name = "sejour_id"),
            inverseJoinColumns = @JoinColumn(name = "animateur_id")
    )
    private List<Utilisateur> equipe;

}
