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

    @ManyToMany
    @JoinTable(
            name = "planning_cellule_horaire",
            joinColumns = @JoinColumn(name = "planning_cellule_id"),
            inverseJoinColumns = @JoinColumn(name = "horaire_id"))
    private Set<Horaire> horaires = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "planning_cellule_moment",
            joinColumns = @JoinColumn(name = "planning_cellule_id"),
            inverseJoinColumns = @JoinColumn(name = "moment_id"))
    private Set<Moment> moments = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "planning_cellule_groupe",
            joinColumns = @JoinColumn(name = "planning_cellule_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id"))
    private Set<Groupe> groupes = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "planning_cellule_lieu",
            joinColumns = @JoinColumn(name = "planning_cellule_id"),
            inverseJoinColumns = @JoinColumn(name = "lieu_id"))
    private Set<Lieu> lieux = new HashSet<>();

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

    public Set<Horaire> getHoraires() {
        return horaires;
    }

    public void setHoraires(Set<Horaire> horaires) {
        this.horaires = horaires;
    }

    public Set<Moment> getMoments() {
        return moments;
    }

    public void setMoments(Set<Moment> moments) {
        this.moments = moments;
    }

    public Set<Groupe> getGroupes() {
        return groupes;
    }

    public void setGroupes(Set<Groupe> groupes) {
        this.groupes = groupes;
    }

    public Set<Lieu> getLieux() {
        return lieux;
    }

    public void setLieux(Set<Lieu> lieux) {
        this.lieux = lieux;
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
