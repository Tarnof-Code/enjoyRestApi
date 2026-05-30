package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;

@Entity
@Table(
        name = "activite_prestataire_non_participation",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_ap_non_participation",
                        columnNames = {"activite_prestataire_id", "utilisateur_id", "moment_id"}))
public class ActivitePrestataireNonParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activite_prestataire_id", nullable = false)
    private ActivitePrestataire activitePrestataire;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "moment_id", nullable = false)
    private Moment moment;

    public ActivitePrestataireNonParticipation() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ActivitePrestataire getActivitePrestataire() {
        return activitePrestataire;
    }

    public void setActivitePrestataire(ActivitePrestataire activitePrestataire) {
        this.activitePrestataire = activitePrestataire;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Moment getMoment() {
        return moment;
    }

    public void setMoment(Moment moment) {
        this.moment = moment;
    }
}
