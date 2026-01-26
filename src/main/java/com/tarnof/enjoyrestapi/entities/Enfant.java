package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Date;
import java.util.List;

import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.NiveauScolaire;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Enfant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    private String nom;
    @NotEmpty(message = "Le champ prénom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    private String prenom;
    @NotNull(message = "Le champ genre ne peut pas être vide.")
    @Enumerated(EnumType.STRING)
    private Genre genre;
    @Temporal(TemporalType.DATE)
    @NotNull(message = "Le champ date de naissance ne peut pas être vide.")  
    private Date dateNaissance;
    @NotNull(message = "Le champ niveau scolaire ne peut pas être vide.")
    @Enumerated(EnumType.STRING)
    private NiveauScolaire niveauScolaire;
    @OneToMany(mappedBy = "enfant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SejourEnfant> sejours;
}
