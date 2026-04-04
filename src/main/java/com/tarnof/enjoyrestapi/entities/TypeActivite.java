package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Types d'activité pour un séjour (référentiel extensible). Les libellés sont uniques par séjour.
 */
@Entity
@Table(
        name = "type_activite",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_type_activite_sejour_libelle", columnNames = {"sejour_id", "libelle"}))
public class TypeActivite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Le libellé du type d'activité est obligatoire")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String libelle;

    /**
     * {@code true} pour les six types insérés automatiquement par séjour (voir {@link
     * com.tarnof.enjoyrestapi.TypeActiviteLibellesParDefaut}) —
     * non modifiables / non supprimables via l'API. {@code false} pour les types créés par {@code POST} sur ce séjour.
     */
    @Column(nullable = false)
    private boolean predefini;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    public TypeActivite() {}

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

    public boolean isPredefini() {
        return predefini;
    }

    public void setPredefini(boolean predefini) {
        this.predefini = predefini;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }
}
