package com.tarnof.enjoyrestapi.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Date;
import java.util.List;

@Entity
public class Utilisateur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "utilisateur_role",
            joinColumns = @JoinColumn(name = "utilisateur_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private List<Role> roles;

    @ManyToMany
    private List<Sejour> sejours;
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Size(min=2,message = "Doit contenir au moins 2 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Format de caractère non autorisé")
    private String nom;
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Size(min=2,message = "Doit contenir au moins 2 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Format de caractère non autorisé")
    private String prenom;
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    private String genre;

    @Size(min=10,max= 10, message = "Doit contenir 10 chiffres")
    @NotEmpty(message = "Le champ numéro de tél. ne peut pas être vide")
    @Pattern(regexp = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$", message = "Format de caractère non autorisé")
    private String telephone;

    @Column(unique = true, nullable = false)
    @NotEmpty(message = "* Le champ email ne peut pas être vide.")
    @jakarta.validation.constraints.Pattern(regexp = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$", message = "* Format de caractère non autorisé")
    private String email;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @NotNull(message = "Le champ date de naissance ne peut pas être vide.")
    private Date dateNaissance;

    @Column(nullable = false)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, une lettre majuscule, une lettre minuscule, un chiffre et un caractère spécial")
    private String motDePasse;

    public Utilisateur(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Role> getRoles() {
        return roles;
    }

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Sejour> getSejours() {
        return sejours;
    }

    public void setSejours(List<Sejour> sejours) {
        this.sejours = sejours;
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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
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

    @Override
    public String toString() {
        return "Utilisateur{" +
                "id=" + id +
                ", roles=" + roles +
                ", sejours=" + sejours +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", genre='" + genre + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", dateNaissance=" + dateNaissance +
                ", motDePasse='" + motDePasse + '\'' +
                '}';
    }
}
