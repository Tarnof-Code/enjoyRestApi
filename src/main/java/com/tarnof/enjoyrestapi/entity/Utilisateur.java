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
    @Id@GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Size(min=2,message = "Doit contenir au moins 2 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Format de caractère non autorisé")
    private String nom;

    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Size(min=2,message = "Doit contenir au moins 2 caractères")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Format de caractère non autorisé")
    private String prenom;

    private enum Genre {
        Garçon,
        Fille
    }
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    private Genre genre;

    @Size(min=10,max= 10, message = "Doit contenir 10 chiffres")
    @NotEmpty(message = "Le champ numéro de tél. ne peut pas être vide")
    @Pattern(regexp = "^[\\+]?[(]?[0-9]{3}[)]?[-\\s\\.]?[0-9]{3}[-\\s\\.]?[0-9]{4,6}$", message = "Format de caractère non autorisé")
    private String telephone;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)
    @NotNull(message = "Le champ date de naissance ne peut pas être vide.")
    private Date dateNaissance;

    @Column(nullable = false)
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
            message = "Le mot de passe doit contenir au moins 8 caractères, une lettre majuscule, une lettre minuscule, un chiffre et un caractère spécial")
    private String motDePasse;
    @ManyToMany(fetch = FetchType.EAGER)
    private Role role;

    private String resetPasswordToken;
    private Date resetPasswordTokenExpiration;
    private boolean isActive;
    private String verificationToken;

    private String photo;

    @CreationTimestamp
    private Date dateCreation;
    @CreationTimestamp
    private Date dateMiseAJour;

    @ManyToOne(fetch = FetchType.EAGER)
    private Chambre chambre;

    @ManyToMany
    private List<Chambre> listeChambresReference;
    @ManyToMany
    private List<Groupe> listeGroupes;
    @ManyToMany
    private List<Activite> listeActivites;



}
