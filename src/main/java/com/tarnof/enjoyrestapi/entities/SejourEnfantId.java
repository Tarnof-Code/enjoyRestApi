package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SejourEnfantId implements Serializable {
    @Column(name = "sejour_id")
    private Integer sejourId;
    @Column(name = "enfant_id")
    private Integer enfantId;

    public SejourEnfantId() {
    }

    public SejourEnfantId(Integer sejourId, Integer enfantId) {
        this.sejourId = sejourId;
        this.enfantId = enfantId;
    }

    public Integer getSejourId() {
        return sejourId;
    }

    public void setSejourId(Integer sejourId) {
        this.sejourId = sejourId;
    }

    public Integer getEnfantId() {
        return enfantId;
    }

    public void setEnfantId(Integer enfantId) {
        this.enfantId = enfantId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SejourEnfantId that = (SejourEnfantId) o;
        return Objects.equals(sejourId, that.sejourId) && Objects.equals(enfantId, that.enfantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sejourId, enfantId);
    }

    @Override
    public String toString() {
        return "SejourEnfantId{" +
                "sejourId=" + sejourId +
                ", enfantId=" + enfantId +
                '}';
    }
}
