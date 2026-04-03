package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

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

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }
}
