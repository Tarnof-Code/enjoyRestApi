package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Entity
@Table(
        name = "horaire",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_horaire_sejour_libelle", columnNames = {"sejour_id", "libelle"}))
public class Horaire {

    /**
     * Heure affichée au format français court, ex. {@code 6h00}, {@code 7h15}, {@code 18h30}
     * (heures 0–23, minutes 00–59).
     */
    public static final String LIBELLE_HORAIRE_PATTERN = "^([0-9]|1\\d|2[0-3])h([0-5]\\d)$";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le libellé d'horaire est obligatoire")
    @Pattern(
            regexp = LIBELLE_HORAIRE_PATTERN,
            message = "L'horaire doit être au format 6h00, 7h15, 18h30, etc. (h entre 0 et 23, minutes sur deux chiffres)")
    @Column(nullable = false, length = 8)
    private String libelle;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    public Horaire() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    /** Sans {@code sejour} : évite lazy load et cycle avec {@link Sejour}. */
    @Override
    public String toString() {
        return "Horaire{" +
                "id=" + id +
                ", libelle='" + libelle + '\'' +
                '}';
    }
}
