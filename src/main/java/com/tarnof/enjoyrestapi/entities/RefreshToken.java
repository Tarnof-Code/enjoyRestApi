package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @OneToOne
    @JoinColumn(name = "utilisateur_id", referencedColumnName = "id")
    private Utilisateur utilisateur;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    public boolean revoked;

    public RefreshToken() {
    }

    public RefreshToken(long id, Utilisateur utilisateur, String token, Instant expiryDate, boolean revoked) {
        this.id = id;
        this.utilisateur = utilisateur;
        this.token = token;
        this.expiryDate = expiryDate;
        this.revoked = revoked;
    }

    public static RefreshTokenBuilder builder() {
        return new RefreshTokenBuilder();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(Instant expiryDate) {
        this.expiryDate = expiryDate;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return id == that.id
                && revoked == that.revoked
                && Objects.equals(utilisateur, that.utilisateur)
                && Objects.equals(token, that.token)
                && Objects.equals(expiryDate, that.expiryDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, utilisateur, token, expiryDate, revoked);
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "id=" + id +
                ", token='" + token + '\'' +
                ", expiryDate=" + expiryDate +
                ", revoked=" + revoked +
                '}';
    }

    public static class RefreshTokenBuilder {
        private long id;
        private Utilisateur utilisateur;
        private String token;
        private Instant expiryDate;
        private boolean revoked;

        public RefreshTokenBuilder id(long id) {
            this.id = id;
            return this;
        }

        public RefreshTokenBuilder utilisateur(Utilisateur utilisateur) {
            this.utilisateur = utilisateur;
            return this;
        }

        public RefreshTokenBuilder token(String token) {
            this.token = token;
            return this;
        }

        public RefreshTokenBuilder expiryDate(Instant expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public RefreshTokenBuilder revoked(boolean revoked) {
            this.revoked = revoked;
            return this;
        }

        public RefreshToken build() {
            return new RefreshToken(id, utilisateur, token, expiryDate, revoked);
        }
    }
}
