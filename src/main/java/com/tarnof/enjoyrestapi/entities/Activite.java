package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Activité planifiée pour un séjour, avec animateurs (membres d'équipe) et lieu optionnel.
 */
@Entity
@Table(name = "activite")
public class Activite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "La date est obligatoire")
    @Column(nullable = false)
    private LocalDate date;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lieu_id")
    private Lieu lieu;

    @ManyToMany
    @JoinTable(
            name = "activite_membre_equipe",
            joinColumns = @JoinColumn(name = "activite_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id"))
    private List<Utilisateur> membres = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "activite_groupe",
            joinColumns = @JoinColumn(name = "activite_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id"))
    private List<Groupe> groupes = new ArrayList<>();

    public Activite() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    public Lieu getLieu() {
        return lieu;
    }

    public void setLieu(Lieu lieu) {
        this.lieu = lieu;
    }

    public List<Utilisateur> getMembres() {
        return membres;
    }

    public void setMembres(List<Utilisateur> membres) {
        this.membres = membres;
    }

    public List<Groupe> getGroupes() {
        return groupes;
    }

    public void setGroupes(List<Groupe> groupes) {
        this.groupes = groupes;
    }
}
