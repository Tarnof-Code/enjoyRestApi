package com.tarnof.enjoyrestapi.entities;

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
    @OneToMany(mappedBy = "sejour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SejourEquipe> equipeRoles;
    @OneToMany(mappedBy = "sejour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SejourEnfant> enfants;
    @OneToMany(mappedBy = "sejour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Groupe> groupes;
    @OneToMany(mappedBy = "sejour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lieu> lieux;
    @OneToMany(mappedBy = "sejour", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Moment> moments;

    public Sejour() {
    }

    public Sejour(int id, String nom, String description, Date dateDebut, Date dateFin, String lieuDuSejour,
                  Utilisateur directeur, List<SejourEquipe> equipeRoles, List<SejourEnfant> enfants,
                  List<Groupe> groupes, List<Lieu> lieux, List<Moment> moments) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lieuDuSejour = lieuDuSejour;
        this.directeur = directeur;
        this.equipeRoles = equipeRoles;
        this.enfants = enfants;
        this.groupes = groupes;
        this.lieux = lieux;
        this.moments = moments;
    }

    public static SejourBuilder builder() {
        return new SejourBuilder();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Date getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(Date dateDebut) {
        this.dateDebut = dateDebut;
    }

    public Date getDateFin() {
        return dateFin;
    }

    public void setDateFin(Date dateFin) {
        this.dateFin = dateFin;
    }

    public String getLieuDuSejour() {
        return lieuDuSejour;
    }

    public void setLieuDuSejour(String lieuDuSejour) {
        this.lieuDuSejour = lieuDuSejour;
    }

    public Utilisateur getDirecteur() {
        return directeur;
    }

    public void setDirecteur(Utilisateur directeur) {
        this.directeur = directeur;
    }

    public List<SejourEquipe> getEquipeRoles() {
        return equipeRoles;
    }

    public void setEquipeRoles(List<SejourEquipe> equipeRoles) {
        this.equipeRoles = equipeRoles;
    }

    public List<SejourEnfant> getEnfants() {
        return enfants;
    }

    public void setEnfants(List<SejourEnfant> enfants) {
        this.enfants = enfants;
    }

    public List<Groupe> getGroupes() {
        return groupes;
    }

    public void setGroupes(List<Groupe> groupes) {
        this.groupes = groupes;
    }

    public List<Lieu> getLieux() {
        return lieux;
    }

    public void setLieux(List<Lieu> lieux) {
        this.lieux = lieux;
    }

    public List<Moment> getMoments() {
        return moments;
    }

    public void setMoments(List<Moment> moments) {
        this.moments = moments;
    }

    @Override
    public String toString() {
        return "Sejour{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", dateDebut=" + dateDebut +
                ", dateFin=" + dateFin +
                ", lieuDuSejour='" + lieuDuSejour + '\'' +
                ", directeur=" + directeur +
                ", equipeRoles=" + equipeRoles +
                ", enfants=" + enfants +
                ", groupes=" + groupes +
                ", lieux=" + lieux +
                ", moments=" + moments +
                '}';
    }

    public static class SejourBuilder {
        private int id;
        private String nom;
        private String description;
        private Date dateDebut;
        private Date dateFin;
        private String lieuDuSejour;
        private Utilisateur directeur;
        private List<SejourEquipe> equipeRoles;
        private List<SejourEnfant> enfants;
        private List<Groupe> groupes;
        private List<Lieu> lieux;
        private List<Moment> moments;

        public SejourBuilder id(int id) {
            this.id = id;
            return this;
        }

        public SejourBuilder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public SejourBuilder description(String description) {
            this.description = description;
            return this;
        }

        public SejourBuilder dateDebut(Date dateDebut) {
            this.dateDebut = dateDebut;
            return this;
        }

        public SejourBuilder dateFin(Date dateFin) {
            this.dateFin = dateFin;
            return this;
        }

        public SejourBuilder lieuDuSejour(String lieuDuSejour) {
            this.lieuDuSejour = lieuDuSejour;
            return this;
        }

        public SejourBuilder directeur(Utilisateur directeur) {
            this.directeur = directeur;
            return this;
        }

        public SejourBuilder equipeRoles(List<SejourEquipe> equipeRoles) {
            this.equipeRoles = equipeRoles;
            return this;
        }

        public SejourBuilder enfants(List<SejourEnfant> enfants) {
            this.enfants = enfants;
            return this;
        }

        public SejourBuilder groupes(List<Groupe> groupes) {
            this.groupes = groupes;
            return this;
        }

        public SejourBuilder lieux(List<Lieu> lieux) {
            this.lieux = lieux;
            return this;
        }

        public SejourBuilder moments(List<Moment> moments) {
            this.moments = moments;
            return this;
        }

        public Sejour build() {
            return new Sejour(id, nom, description, dateDebut, dateFin, lieuDuSejour, directeur, equipeRoles, enfants, groupes, lieux, moments);
        }
    }
}
