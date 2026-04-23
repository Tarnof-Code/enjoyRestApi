package com.tarnof.enjoyrestapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Entity
public class Utilisateur implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "token_id", unique = true)
    private String tokenId;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<SejourEquipe> sejoursEquipe;
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    private String nom;
    @NotEmpty(message = "Le champ prénom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    private String prenom;
    @NotNull(message = "Le champ genre ne peut pas être vide.")
    @Enumerated(EnumType.STRING)
    private Genre genre;
    @Column(unique = true)
    @NotEmpty(message = "Le champ N° de téléphone ne peut pas être vide.")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Numéro de téléphone non valide")
    private String telephone;
    @Column(unique = true)
    @NotEmpty(message = "Le champ email ne peut pas être vide.")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email non valide")
    private String email;
    @Temporal(TemporalType.DATE)
    @NotNull(message = "Le champ date de naissance ne peut pas être vide.")
    private Date dateNaissance;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*!]).{4,}$", message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, et un caractère spécial, et comporter au moins 4 caractères")
    private String motDePasse;
    @Transient
    private Instant dateExpirationCompte;
    @JsonIgnore
    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private RefreshToken refreshToken;
    @ManyToMany(mappedBy = "referents")
    private List<Groupe> groupesReferent = new ArrayList<>();

    public Utilisateur() {
    }

    public static UtilisateurBuilder builder() {
        return new UtilisateurBuilder();
    }

    public UtilisateurBuilder toBuilder() {
        return new UtilisateurBuilder()
                .id(this.id)
                .tokenId(this.tokenId)
                .role(this.role)
                .sejoursEquipe(this.sejoursEquipe)
                .nom(this.nom)
                .prenom(this.prenom)
                .genre(this.genre)
                .telephone(this.telephone)
                .email(this.email)
                .dateNaissance(this.dateNaissance)
                .motDePasse(this.motDePasse)
                .dateExpirationCompte(this.dateExpirationCompte)
                .refreshToken(this.refreshToken)
                .groupesReferent(this.groupesReferent);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<SejourEquipe> getSejoursEquipe() {
        return sejoursEquipe;
    }

    public void setSejoursEquipe(List<SejourEquipe> sejoursEquipe) {
        this.sejoursEquipe = sejoursEquipe;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(Date dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public void setDateExpirationCompte(Instant dateExpirationCompte) {
        this.dateExpirationCompte = dateExpirationCompte;
    }

    public RefreshToken getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(RefreshToken refreshToken) {
        this.refreshToken = refreshToken;
    }

    public List<Groupe> getGroupesReferent() {
        return groupesReferent;
    }

    public void setGroupesReferent(List<Groupe> groupesReferent) {
        this.groupesReferent = groupesReferent;
    }

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", tokenId='" + tokenId + '\'' +
                ", role=" + role +
                ", sejoursEquipe=" + sejoursEquipe +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", genre=" + genre +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", motDePasse='" + motDePasse + '\'' +
                ", dateExpirationCompte=" + dateExpirationCompte +
                ", refreshToken=" + refreshToken +
                '}';
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() { return motDePasse; }
    @Override
    public String getUsername() {
        return tokenId;
    }
    @Override
    public boolean isAccountNonExpired() {
        Instant expiryDate = refreshToken.getExpiryDate();
        Instant currentDate = Instant.now();
        return !expiryDate.isBefore(currentDate);
    }

    @Transient
    public Instant getDateExpirationCompte(){
        if (refreshToken != null && refreshToken.getExpiryDate() != null) {
            return refreshToken.getExpiryDate();
        }
        return null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }

    public static class UtilisateurBuilder {
        private int id;
        private String tokenId;
        private Role role;
        private List<SejourEquipe> sejoursEquipe;
        private String nom;
        private String prenom;
        private Genre genre;
        private String telephone;
        private String email;
        private Date dateNaissance;
        private String motDePasse;
        private Instant dateExpirationCompte;
        private RefreshToken refreshToken;
        private List<Groupe> groupesReferent;

        public UtilisateurBuilder id(int id) {
            this.id = id;
            return this;
        }

        public UtilisateurBuilder tokenId(String tokenId) {
            this.tokenId = tokenId;
            return this;
        }

        public UtilisateurBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UtilisateurBuilder sejoursEquipe(List<SejourEquipe> sejoursEquipe) {
            this.sejoursEquipe = sejoursEquipe;
            return this;
        }

        public UtilisateurBuilder nom(String nom) {
            this.nom = nom;
            return this;
        }

        public UtilisateurBuilder prenom(String prenom) {
            this.prenom = prenom;
            return this;
        }

        public UtilisateurBuilder genre(Genre genre) {
            this.genre = genre;
            return this;
        }

        public UtilisateurBuilder telephone(String telephone) {
            this.telephone = telephone;
            return this;
        }

        public UtilisateurBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UtilisateurBuilder dateNaissance(Date dateNaissance) {
            this.dateNaissance = dateNaissance;
            return this;
        }

        public UtilisateurBuilder motDePasse(String motDePasse) {
            this.motDePasse = motDePasse;
            return this;
        }

        public UtilisateurBuilder dateExpirationCompte(Instant dateExpirationCompte) {
            this.dateExpirationCompte = dateExpirationCompte;
            return this;
        }

        public UtilisateurBuilder refreshToken(RefreshToken refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public UtilisateurBuilder groupesReferent(List<Groupe> groupesReferent) {
            this.groupesReferent = groupesReferent;
            return this;
        }

        public Utilisateur build() {
            Utilisateur u = new Utilisateur();
            u.setId(id);
            u.setTokenId(tokenId);
            u.setRole(role);
            u.setSejoursEquipe(sejoursEquipe);
            u.setNom(nom);
            u.setPrenom(prenom);
            u.setGenre(genre);
            u.setTelephone(telephone);
            u.setEmail(email);
            u.setDateNaissance(dateNaissance);
            u.setMotDePasse(motDePasse);
            u.setDateExpirationCompte(dateExpirationCompte);
            u.setRefreshToken(refreshToken);
            u.setGroupesReferent(groupesReferent != null ? groupesReferent : new ArrayList<>());
            return u;
        }
    }
}
