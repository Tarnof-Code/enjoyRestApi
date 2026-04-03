package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.NiveauScolaire;

@Entity
public class Enfant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    private String nom;
    @NotEmpty(message = "Le champ prénom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    private String prenom;
    @NotNull(message = "Le champ genre ne peut pas être vide.")
    @Enumerated(EnumType.STRING)
    private Genre genre;
    @Temporal(TemporalType.DATE)
    @NotNull(message = "Le champ date de naissance ne peut pas être vide.")
    private Date dateNaissance;
    @NotNull(message = "Le champ niveau scolaire ne peut pas être vide.")
    @Enumerated(EnumType.STRING)
    private NiveauScolaire niveauScolaire;
    @OneToOne(mappedBy = "enfant", cascade = CascadeType.ALL, orphanRemoval = true)
    private DossierEnfant dossier;
    @OneToMany(mappedBy = "enfant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SejourEnfant> sejours;
    @ManyToMany(mappedBy = "enfants")
    private List<Groupe> groupes;

    public Enfant() {
    }

    public Enfant(int id, String nom, String prenom, Genre genre, Date dateNaissance, NiveauScolaire niveauScolaire,
                  DossierEnfant dossier, List<SejourEnfant> sejours, List<Groupe> groupes) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.genre = genre;
        this.dateNaissance = dateNaissance;
        this.niveauScolaire = niveauScolaire;
        this.dossier = dossier;
        this.sejours = sejours;
        this.groupes = groupes;
    }

    public static EnfantBuilder builder() {
        return new EnfantBuilder();
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

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public NiveauScolaire getNiveauScolaire() {
        return niveauScolaire;
    }

    public void setNiveauScolaire(NiveauScolaire niveauScolaire) {
        this.niveauScolaire = niveauScolaire;
    }

    public DossierEnfant getDossier() {
        return dossier;
    }

    public void setDossier(DossierEnfant dossier) {
        this.dossier = dossier;
    }

    public List<SejourEnfant> getSejours() {
        return sejours;
    }

    public void setSejours(List<SejourEnfant> sejours) {
        this.sejours = sejours;
    }

    public List<Groupe> getGroupes() {
        return groupes;
    }

    public void setGroupes(List<Groupe> groupes) {
        this.groupes = groupes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Enfant enfant = (Enfant) o;
        return id == enfant.id
                && Objects.equals(nom, enfant.nom)
                && Objects.equals(prenom, enfant.prenom)
                && genre == enfant.genre
                && Objects.equals(dateNaissance, enfant.dateNaissance)
                && niveauScolaire == enfant.niveauScolaire;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, genre, dateNaissance, niveauScolaire);
    }

    @Override
    public String toString() {
        return "Enfant{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", genre=" + genre +
                ", dateNaissance=" + dateNaissance +
                ", niveauScolaire=" + niveauScolaire +
                '}';
    }

    public static class EnfantBuilder {
        private int id;
        private String nom;
        private String prenom;
        private Genre genre;
        private Date dateNaissance;
        private NiveauScolaire niveauScolaire;
        private DossierEnfant dossier;
        private List<SejourEnfant> sejours;
        private List<Groupe> groupes;

        public EnfantBuilder id(int id) {
            this.id = id;
            return this;
        }

        public EnfantBuilder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public EnfantBuilder prenom(String prenom) {
            this.prenom = prenom;
            return this;
        }

        public EnfantBuilder genre(Genre genre) {
            this.genre = genre;
            return this;
        }

        public EnfantBuilder dateNaissance(Date dateNaissance) {
            this.dateNaissance = dateNaissance;
            return this;
        }

        public EnfantBuilder niveauScolaire(NiveauScolaire niveauScolaire) {
            this.niveauScolaire = niveauScolaire;
            return this;
        }

        public EnfantBuilder dossier(DossierEnfant dossier) {
            this.dossier = dossier;
            return this;
        }

        public EnfantBuilder sejours(List<SejourEnfant> sejours) {
            this.sejours = sejours;
            return this;
        }

        public EnfantBuilder groupes(List<Groupe> groupes) {
            this.groupes = groupes;
            return this;
        }

        public Enfant build() {
            return new Enfant(id, nom, prenom, genre, dateNaissance, niveauScolaire, dossier, sejours, groupes);
        }
    }
}
