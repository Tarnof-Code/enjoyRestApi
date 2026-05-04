package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.TypeReferenceAlimentaire;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(
        name = "reference_alimentaire",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_reference_alimentaire_type_libelle",
                        columnNames = {"type", "libelle"}))
public class ReferenceAlimentaire {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TypeReferenceAlimentaire type;

    @NotBlank(message = "Le libellé est obligatoire")
    @Column(nullable = false, length = 255)
    private String libelle;

    /** Ordre d’affichage dans les listes ; les plus petits en premier. */
    @Column(name = "ordre")
    private Integer ordre;

    @Column(nullable = false)
    private boolean actif = true;

    public ReferenceAlimentaire() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TypeReferenceAlimentaire getType() {
        return type;
    }

    public void setType(TypeReferenceAlimentaire type) {
        this.type = type;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean actif) {
        this.actif = actif;
    }
}
