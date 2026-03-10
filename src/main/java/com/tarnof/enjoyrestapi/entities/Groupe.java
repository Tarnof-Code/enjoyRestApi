package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.NiveauScolaire;
import com.tarnof.enjoyrestapi.enums.TypeGroupe;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Groupe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @NotBlank(message = "Le nom du groupe est obligatoire")
    private String nom;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NotNull(message = "Le type de groupe est obligatoire")
    @Enumerated(EnumType.STRING)
    private TypeGroupe typeGroupe;
    
    private Integer ageMin;
    private Integer ageMax;
    
    @Enumerated(EnumType.STRING)
    private NiveauScolaire niveauScolaireMin;
    @Enumerated(EnumType.STRING)
    private NiveauScolaire niveauScolaireMax;
    
    @ManyToOne
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;
    
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(name = "groupe_enfant",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "enfant_id"))
    @Builder.Default
    private List<Enfant> enfants = new ArrayList<>();
    
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(name = "groupe_referent",
            joinColumns = @JoinColumn(name = "groupe_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id"))
    @Builder.Default
    private List<Utilisateur> referents = new ArrayList<>();
}