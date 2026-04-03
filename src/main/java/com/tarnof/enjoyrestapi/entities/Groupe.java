package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.NiveauScolaire;
import com.tarnof.enjoyrestapi.enums.TypeGroupe;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class Groupe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le nom du groupe est obligatoire")
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Le type de groupe est obligatoire")
    @Enumerated(EnumType.STRING)
    private TypeGroupe typeGroupe;

    private Integer ageMin;
    private Integer ageMax;

    @Enumerated(EnumType.STRING)
    private NiveauScolaire niveauScolaireMin;
    @Enumerated(EnumType.STRING)
    private NiveauScolaire niveauScolaireMax;

    @ManyToOne
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    @ManyToMany
    @JoinTable(name = "groupe_enfant",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "enfant_id"))
    private List<Enfant> enfants = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "groupe_referent",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id"))
    private List<Utilisateur> referents = new ArrayList<>();

    public Groupe() {
    }

    public Groupe(Integer id, String nom, String description, TypeGroupe typeGroupe, Integer ageMin, Integer ageMax,
                  NiveauScolaire niveauScolaireMin, NiveauScolaire niveauScolaireMax, Sejour sejour,
                  List<Enfant> enfants, List<Utilisateur> referents) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.typeGroupe = typeGroupe;
        this.ageMin = ageMin;
        this.ageMax = ageMax;
        this.niveauScolaireMin = niveauScolaireMin;
        this.niveauScolaireMax = niveauScolaireMax;
        this.sejour = sejour;
        if (enfants != null) {
            this.enfants = enfants;
        }
        if (referents != null) {
            this.referents = referents;
        }
    }

    public static GroupeBuilder builder() {
        return new GroupeBuilder();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TypeGroupe getTypeGroupe() {
        return typeGroupe;
    }

    public void setTypeGroupe(TypeGroupe typeGroupe) {
        this.typeGroupe = typeGroupe;
    }

    public Integer getAgeMin() {
        return ageMin;
    }

    public void setAgeMin(Integer ageMin) {
        this.ageMin = ageMin;
    }

    public Integer getAgeMax() {
        return ageMax;
    }

    public void setAgeMax(Integer ageMax) {
        this.ageMax = ageMax;
    }

    public NiveauScolaire getNiveauScolaireMin() {
        return niveauScolaireMin;
    }

    public void setNiveauScolaireMin(NiveauScolaire niveauScolaireMin) {
        this.niveauScolaireMin = niveauScolaireMin;
    }

    public NiveauScolaire getNiveauScolaireMax() {
        return niveauScolaireMax;
    }

    public void setNiveauScolaireMax(NiveauScolaire niveauScolaireMax) {
        this.niveauScolaireMax = niveauScolaireMax;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    public List<Enfant> getEnfants() {
        return enfants;
    }

    public void setEnfants(List<Enfant> enfants) {
        this.enfants = enfants;
    }

    public List<Utilisateur> getReferents() {
        return referents;
    }

    public void setReferents(List<Utilisateur> referents) {
        this.referents = referents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Groupe groupe = (Groupe) o;
        return Objects.equals(id, groupe.id)
                && Objects.equals(nom, groupe.nom)
                && Objects.equals(description, groupe.description)
                && typeGroupe == groupe.typeGroupe
                && Objects.equals(ageMin, groupe.ageMin)
                && Objects.equals(ageMax, groupe.ageMax)
                && Objects.equals(niveauScolaireMin, groupe.niveauScolaireMin)
                && Objects.equals(niveauScolaireMax, groupe.niveauScolaireMax)
                && Objects.equals(sejour, groupe.sejour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, description, typeGroupe, ageMin, ageMax, niveauScolaireMin, niveauScolaireMax, sejour);
    }

    @Override
    public String toString() {
        return "Groupe{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", description='" + description + '\'' +
                ", typeGroupe=" + typeGroupe +
                ", ageMin=" + ageMin +
                ", ageMax=" + ageMax +
                ", niveauScolaireMin=" + niveauScolaireMin +
                ", niveauScolaireMax=" + niveauScolaireMax +
                ", sejour=" + sejour +
                '}';
    }

    public static class GroupeBuilder {
        private Integer id;
        private String nom;
        private String description;
        private TypeGroupe typeGroupe;
        private Integer ageMin;
        private Integer ageMax;
        private NiveauScolaire niveauScolaireMin;
        private NiveauScolaire niveauScolaireMax;
        private Sejour sejour;
        private List<Enfant> enfants;
        private List<Utilisateur> referents;

        public GroupeBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public GroupeBuilder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public GroupeBuilder description(String description) {
            this.description = description;
            return this;
        }

        public GroupeBuilder typeGroupe(TypeGroupe typeGroupe) {
            this.typeGroupe = typeGroupe;
            return this;
        }

        public GroupeBuilder ageMin(Integer ageMin) {
            this.ageMin = ageMin;
            return this;
        }

        public GroupeBuilder ageMax(Integer ageMax) {
            this.ageMax = ageMax;
            return this;
        }

        public GroupeBuilder niveauScolaireMin(NiveauScolaire niveauScolaireMin) {
            this.niveauScolaireMin = niveauScolaireMin;
            return this;
        }

        public GroupeBuilder niveauScolaireMax(NiveauScolaire niveauScolaireMax) {
            this.niveauScolaireMax = niveauScolaireMax;
            return this;
        }

        public GroupeBuilder sejour(Sejour sejour) {
            this.sejour = sejour;
            return this;
        }

        public GroupeBuilder enfants(List<Enfant> enfants) {
            this.enfants = enfants;
            return this;
        }

        public GroupeBuilder referents(List<Utilisateur> referents) {
            this.referents = referents;
            return this;
        }

        public Groupe build() {
            List<Enfant> e = enfants != null ? enfants : new ArrayList<>();
            List<Utilisateur> r = referents != null ? referents : new ArrayList<>();
            return new Groupe(id, nom, description, typeGroupe, ageMin, ageMax, niveauScolaireMin, niveauScolaireMax, sejour, e, r);
        }
    }
}
