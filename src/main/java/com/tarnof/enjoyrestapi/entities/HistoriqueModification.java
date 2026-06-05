package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.HistoriqueModificationAction;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING, length = 32)
@Table(
        name = "historique_modification",
        indexes = {
            @Index(
                    name = "idx_hist_mod_ligne_jour",
                    columnList = "planning_ligne_id,planning_jour,date_modification"),
            @Index(name = "idx_hist_mod_activite", columnList = "activite_id,date_modification"),
            @Index(
                    name = "idx_hist_mod_cahier_inf",
                    columnList = "cahier_infirmerie_entree_id,date_modification"),
            @Index(name = "idx_hist_mod_chambre", columnList = "chambre_id,date_modification")
        })
public abstract class HistoriqueModification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 32)
    private HistoriqueModificationAction action;

    @Column(name = "date_modification", nullable = false)
    private Instant dateModification;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "modificateur_id", nullable = false)
    private Utilisateur modificateur;

    @Column(name = "ancienne_valeur", columnDefinition = "TEXT")
    private String ancienneValeur;

    @Column(name = "nouvelle_valeur", columnDefinition = "TEXT")
    private String nouvelleValeur;

    protected HistoriqueModification() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public HistoriqueModificationAction getAction() {
        return action;
    }

    public void setAction(HistoriqueModificationAction action) {
        this.action = action;
    }

    public Instant getDateModification() {
        return dateModification;
    }

    public void setDateModification(Instant dateModification) {
        this.dateModification = dateModification;
    }

    public Utilisateur getModificateur() {
        return modificateur;
    }

    public void setModificateur(Utilisateur modificateur) {
        this.modificateur = modificateur;
    }

    public String getAncienneValeur() {
        return ancienneValeur;
    }

    public void setAncienneValeur(String ancienneValeur) {
        this.ancienneValeur = ancienneValeur;
    }

    public String getNouvelleValeur() {
        return nouvelleValeur;
    }

    public void setNouvelleValeur(String nouvelleValeur) {
        this.nouvelleValeur = nouvelleValeur;
    }
}
