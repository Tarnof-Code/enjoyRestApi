package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.PlanningLigneLibelleSource;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planning_grille")
public class PlanningGrille {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    @Column(nullable = false, length = 255)
    private String titre;

    @Column(name = "consigne_globale", columnDefinition = "TEXT")
    private String consigneGlobale;

    /**
     * Type de libellé imposé à toutes les lignes (saisie libre, groupe, lieu, horaire, moment).
     * {@code null} = aucun type choisi : pas de colonne libellé de ligne (ni saisie libre ni référence métier).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "source_libelle_lignes", length = 32)
    private PlanningLigneLibelleSource sourceLibelleLignes;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_contenu_cellules", nullable = false, length = 32)
    private PlanningLigneLibelleSource sourceContenuCellules = PlanningLigneLibelleSource.SAISIE_LIBRE;

    @Column(name = "mise_a_jour", nullable = false)
    private Instant miseAJour = Instant.now();

    @OneToMany(mappedBy = "grille", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanningLigne> lignes = new ArrayList<>();

    public PlanningGrille() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getConsigneGlobale() {
        return consigneGlobale;
    }

    public void setConsigneGlobale(String consigneGlobale) {
        this.consigneGlobale = consigneGlobale;
    }

    public PlanningLigneLibelleSource getSourceLibelleLignes() {
        return sourceLibelleLignes;
    }

    public void setSourceLibelleLignes(PlanningLigneLibelleSource sourceLibelleLignes) {
        this.sourceLibelleLignes = sourceLibelleLignes;
    }

    public PlanningLigneLibelleSource getSourceContenuCellules() {
        return sourceContenuCellules;
    }

    public void setSourceContenuCellules(PlanningLigneLibelleSource sourceContenuCellules) {
        this.sourceContenuCellules = sourceContenuCellules;
    }

    public Instant getMiseAJour() {
        return miseAJour;
    }

    public void setMiseAJour(Instant miseAJour) {
        this.miseAJour = miseAJour;
    }

    public List<PlanningLigne> getLignes() {
        return lignes;
    }

    public void setLignes(List<PlanningLigne> lignes) {
        this.lignes = lignes;
    }

    /** Sans collections lazy : debug uniquement. */
    @Override
    public String toString() {
        return "PlanningGrille{" + "id=" + id + ", titre='" + titre + '\'' + '}';
    }
}
