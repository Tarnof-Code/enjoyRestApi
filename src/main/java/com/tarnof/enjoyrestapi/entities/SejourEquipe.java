package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.RoleSejour;
import jakarta.persistence.*;

@Entity
@Table(name = "sejour_equipe")
public class SejourEquipe {
    @EmbeddedId
    private SejourEquipeId id;

    @MapsId("sejourId")
    @ManyToOne
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    @MapsId("utilisateurId")
    @ManyToOne
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleSejour roleSejour;

    public SejourEquipe() {
    }

    public SejourEquipe(Sejour sejour, Utilisateur utilisateur, RoleSejour roleSejour) {
        this.id = new SejourEquipeId();
        this.sejour = sejour;
        this.utilisateur = utilisateur;
        this.roleSejour = roleSejour;
    }

    public static SejourEquipeBuilder builder() {
        return new SejourEquipeBuilder();
    }

    public SejourEquipeId getId() {
        return id;
    }

    public void setId(SejourEquipeId id) {
        this.id = id;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public RoleSejour getRoleSejour() {
        return roleSejour;
    }

    public void setRoleSejour(RoleSejour roleSejour) {
        this.roleSejour = roleSejour;
    }

    @Override
    public String toString() {
        return "SejourEquipe{" +
                "id=" + id +
                ", sejour=" + sejour +
                ", utilisateur=" + utilisateur +
                ", roleSejour=" + roleSejour +
                '}';
    }

    public static class SejourEquipeBuilder {
        private Sejour sejour;
        private Utilisateur utilisateur;
        private RoleSejour roleSejour;

        public SejourEquipeBuilder sejour(Sejour sejour) {
            this.sejour = sejour;
            return this;
        }

        public SejourEquipeBuilder utilisateur(Utilisateur utilisateur) {
            this.utilisateur = utilisateur;
            return this;
        }

        public SejourEquipeBuilder roleSejour(RoleSejour roleSejour) {
            this.roleSejour = roleSejour;
            return this;
        }

        public SejourEquipe build() {
            return new SejourEquipe(sejour, utilisateur, roleSejour);
        }
    }
}
