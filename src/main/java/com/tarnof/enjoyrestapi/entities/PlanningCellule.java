package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "planning_cellule",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_planning_cellule_ligne_jour", columnNames = {"ligne_id", "jour"}))
public class PlanningCellule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ligne_id", nullable = false)
    private PlanningLigne ligne;

    @Column(nullable = false)
    private LocalDate jour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "horaire_id")
    private Horaire horaire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moment_id")
    private Moment moment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id")
    private Groupe groupe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieu_id")
    private Lieu lieu;

    @Column(name = "texte_libre", length = 1024)
    private String texteLibre;

    @ManyToMany
    @JoinTable(
            name = "planning_cellule_utilisateur",
            joinColumns = @JoinColumn(name = "planning_cellule_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_token_id", referencedColumnName = "token_id"))
    private Set<Utilisateur> animateursAssignes = new HashSet<>();

    public PlanningCellule() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PlanningLigne getLigne() {
        return ligne;
    }

    public void setLigne(PlanningLigne ligne) {
        this.ligne = ligne;
    }

    public LocalDate getJour() {
        return jour;
    }

    public void setJour(LocalDate jour) {
        this.jour = jour;
    }

    public Horaire getHoraire() {
        return horaire;
    }

    public void setHoraire(Horaire horaire) {
        this.horaire = horaire;
    }

    public Moment getMoment() {
        return moment;
    }

    public void setMoment(Moment moment) {
        this.moment = moment;
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }

    public Lieu getLieu() {
        return lieu;
    }

    public void setLieu(Lieu lieu) {
        this.lieu = lieu;
    }

    public String getTexteLibre() {
        return texteLibre;
    }

    public void setTexteLibre(String texteLibre) {
        this.texteLibre = texteLibre;
    }

    public Set<Utilisateur> getAnimateursAssignes() {
        return animateursAssignes;
    }

    public void setAnimateursAssignes(Set<Utilisateur> animateursAssignes) {
        this.animateursAssignes = animateursAssignes;
    }

    @Override
    public String toString() {
        return "PlanningCellule{" + "id=" + id + ", jour=" + jour + '}';
    }
}
