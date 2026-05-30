package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;

@Entity
@Table(
        name = "chambre_occupant",
        uniqueConstraints = {
            @UniqueConstraint(name = "uk_chambre_occupant_enfant", columnNames = {"enfant_id"}),
            @UniqueConstraint(name = "uk_chambre_occupant_utilisateur", columnNames = {"utilisateur_id"})
        })
public class ChambreOccupant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chambre_id", nullable = false)
    private Chambre chambre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enfant_id")
    private Enfant enfant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(name = "numero_lit")
    private Integer numeroLit;

    public ChambreOccupant() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Chambre getChambre() {
        return chambre;
    }

    public void setChambre(Chambre chambre) {
        this.chambre = chambre;
    }

    public Enfant getEnfant() {
        return enfant;
    }

    public void setEnfant(Enfant enfant) {
        this.enfant = enfant;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Integer getNumeroLit() {
        return numeroLit;
    }

    public void setNumeroLit(Integer numeroLit) {
        this.numeroLit = numeroLit;
    }
}
