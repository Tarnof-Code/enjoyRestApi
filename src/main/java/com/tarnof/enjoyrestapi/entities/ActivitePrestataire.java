package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Activité externalisée (prestataire) planifiée pour un séjour, avec groupes optionnels.
 */
@Entity
@Table(name = "activite_prestataire")
public class ActivitePrestataire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le nom est obligatoire")
    @Column(nullable = false)
    private String nom;

    @NotNull(message = "La date est obligatoire")
    @Column(nullable = false)
    private LocalDate date;

    @ManyToMany
    @JoinTable(
            name = "activite_prestataire_moment",
            joinColumns = @JoinColumn(name = "activite_prestataire_id"),
            inverseJoinColumns = @JoinColumn(name = "moment_id"))
    private List<Moment> moments = new ArrayList<>();

    @Column(name = "heure_depart")
    private LocalTime heureDepart;

    @Column(name = "heure_retour")
    private LocalTime heureRetour;

    @Column(columnDefinition = "TEXT")
    private String informations;

    @Column(length = 30)
    private String telephone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    @ManyToMany
    @JoinTable(
            name = "activite_prestataire_groupe",
            joinColumns = @JoinColumn(name = "activite_prestataire_id"),
            inverseJoinColumns = @JoinColumn(name = "groupe_id"))
    private List<Groupe> groupes = new ArrayList<>();

    @OneToMany(mappedBy = "activitePrestataire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ActivitePrestataireNonParticipation> nonParticipations = new ArrayList<>();

    public ActivitePrestataire() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<Moment> getMoments() {
        return moments;
    }

    public void setMoments(List<Moment> moments) {
        this.moments = moments;
    }

    public LocalTime getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(LocalTime heureDepart) {
        this.heureDepart = heureDepart;
    }

    public LocalTime getHeureRetour() {
        return heureRetour;
    }

    public void setHeureRetour(LocalTime heureRetour) {
        this.heureRetour = heureRetour;
    }

    public String getInformations() {
        return informations;
    }

    public void setInformations(String informations) {
        this.informations = informations;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    public List<Groupe> getGroupes() {
        return groupes;
    }

    public void setGroupes(List<Groupe> groupes) {
        this.groupes = groupes;
    }

    public List<ActivitePrestataireNonParticipation> getNonParticipations() {
        return nonParticipations;
    }

    public void setNonParticipations(List<ActivitePrestataireNonParticipation> nonParticipations) {
        this.nonParticipations = nonParticipations;
    }

    @Override
    public String toString() {
        return "ActivitePrestataire{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", date=" + date +
                '}';
    }
}
