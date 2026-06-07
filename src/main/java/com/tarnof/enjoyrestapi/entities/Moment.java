package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "moment",
        uniqueConstraints = @UniqueConstraint(name = "uk_moment_sejour_nom", columnNames = {"sejour_id", "nom"}))
public class Moment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le nom du moment est obligatoire")
    @Column(nullable = false)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    /**
     * Moment parent (groupe) auquel ce moment est rattaché, ou {@code null} pour un moment racine.
     * Hiérarchie limitée à deux niveaux : un parent ne peut pas avoir lui-même de parent.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Moment parent;

    @OneToMany(mappedBy = "parent")
    private List<Moment> enfants = new ArrayList<>();

    /**
     * Position du moment dans la journée pour ce séjour (0 = premier). Nullable pour compatibilité
     * des lignes créées avant ce champ ; le tri utilise {@code COALESCE(ordre, id)}.
     */
    @Column(name = "ordre")
    private Integer ordre;

    public Moment() {
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

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    public Moment getParent() {
        return parent;
    }

    public void setParent(Moment parent) {
        this.parent = parent;
    }

    public List<Moment> getEnfants() {
        return enfants;
    }

    public void setEnfants(List<Moment> enfants) {
        this.enfants = enfants;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    /** Sans {@code sejour} : évite lazy load et cycle avec {@link Sejour}. */
    @Override
    public String toString() {
        return "Moment{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", ordre=" + ordre +
                '}';
    }
}
