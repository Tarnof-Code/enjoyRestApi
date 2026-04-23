package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "planning_ligne")
public class PlanningLigne {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grille_id", nullable = false)
    private PlanningGrille grille;

    @Column(nullable = false)
    private Integer ordre;

    /**
     * Libellé de section partagé par plusieurs lignes (ex. « 7h45 – Levers »), affichage / tri.
     */
    @Column(name = "libelle_regroupement", length = 512)
    private String libelleRegroupement;

    /**
     * Texte d’en-tête de ligne (surtout pour {@code sourceLibelleLignes SAISIE_LIBRE}, ou libellé complémentaire optionnel
     * pour les autres types).
     */
    @Column(name = "libelle_saisie_libre", length = 512)
    private String libelleSaisieLibre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libelle_moment_id")
    private Moment libelleMoment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libelle_horaire_id")
    private Horaire libelleHoraire;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libelle_groupe_id")
    private Groupe libelleGroupe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libelle_lieu_id")
    private Lieu libelleLieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "libelle_utilisateur_token_id", referencedColumnName = "token_id")
    private Utilisateur libelleUtilisateur;

    @OneToMany(mappedBy = "ligne", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PlanningCellule> cellules = new ArrayList<>();

    public PlanningLigne() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public PlanningGrille getGrille() {
        return grille;
    }

    public void setGrille(PlanningGrille grille) {
        this.grille = grille;
    }

    public Integer getOrdre() {
        return ordre;
    }

    public void setOrdre(Integer ordre) {
        this.ordre = ordre;
    }

    public String getLibelleRegroupement() {
        return libelleRegroupement;
    }

    public void setLibelleRegroupement(String libelleRegroupement) {
        this.libelleRegroupement = libelleRegroupement;
    }

    public String getLibelleSaisieLibre() {
        return libelleSaisieLibre;
    }

    public void setLibelleSaisieLibre(String libelleSaisieLibre) {
        this.libelleSaisieLibre = libelleSaisieLibre;
    }

    public Moment getLibelleMoment() {
        return libelleMoment;
    }

    public void setLibelleMoment(Moment libelleMoment) {
        this.libelleMoment = libelleMoment;
    }

    public Horaire getLibelleHoraire() {
        return libelleHoraire;
    }

    public void setLibelleHoraire(Horaire libelleHoraire) {
        this.libelleHoraire = libelleHoraire;
    }

    public Groupe getLibelleGroupe() {
        return libelleGroupe;
    }

    public void setLibelleGroupe(Groupe libelleGroupe) {
        this.libelleGroupe = libelleGroupe;
    }

    public Lieu getLibelleLieu() {
        return libelleLieu;
    }

    public void setLibelleLieu(Lieu libelleLieu) {
        this.libelleLieu = libelleLieu;
    }

    public Utilisateur getLibelleUtilisateur() {
        return libelleUtilisateur;
    }

    public void setLibelleUtilisateur(Utilisateur libelleUtilisateur) {
        this.libelleUtilisateur = libelleUtilisateur;
    }

    public List<PlanningCellule> getCellules() {
        return cellules;
    }

    public void setCellules(List<PlanningCellule> cellules) {
        this.cellules = cellules;
    }

    @Override
    public String toString() {
        return "PlanningLigne{"
                + "id="
                + id
                + ", libelleSaisieLibre='"
                + libelleSaisieLibre
                + '\''
                + ", ordre="
                + ordre
                + '}';
    }
}
