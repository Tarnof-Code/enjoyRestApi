package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SejourEquipeId implements Serializable {
    @Column(name = "sejour_id")
    private Integer sejourId;

    @Column(name = "utilisateur_id")
    private Integer utilisateurId;

    public SejourEquipeId() {
    }

    public SejourEquipeId(Integer sejourId, Integer utilisateurId) {
        this.sejourId = sejourId;
        this.utilisateurId = utilisateurId;
    }

    public Integer getSejourId() {
        return sejourId;
    }

    public void setSejourId(Integer sejourId) {
        this.sejourId = sejourId;
    }

    public Integer getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(Integer utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SejourEquipeId that = (SejourEquipeId) o;
        return Objects.equals(sejourId, that.sejourId) && Objects.equals(utilisateurId, that.utilisateurId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sejourId, utilisateurId);
    }

    @Override
    public String toString() {
        return "SejourEquipeId{" +
                "sejourId=" + sejourId +
                ", utilisateurId=" + utilisateurId +
                '}';
    }
}
