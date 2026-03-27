package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.EmplacementLieu;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "lieu",
        uniqueConstraints = @UniqueConstraint(name = "uk_lieu_sejour_nom", columnNames = {"sejour_id", "nom"})
)
public class Lieu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le nom du lieu est obligatoire")
    private String nom;

    @NotNull(message = "L'emplacement (intérieur / extérieur / hors centre) est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmplacementLieu emplacement;

    /** Capacité maximale optionnelle (nombre de personnes, places, etc.). */
    private Integer nombreMax;

    @ManyToOne
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    public Lieu() {
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

    public EmplacementLieu getEmplacement() {
        return emplacement;
    }

    public void setEmplacement(EmplacementLieu emplacement) {
        this.emplacement = emplacement;
    }

    public Integer getNombreMax() {
        return nombreMax;
    }

    public void setNombreMax(Integer nombreMax) {
        this.nombreMax = nombreMax;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }
}
