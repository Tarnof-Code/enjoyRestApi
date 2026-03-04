package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DossierEnfant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    @JoinColumn(name = "enfant_id", nullable = false)
    private Enfant enfant;
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email non valide")
    private String emailParent1;
    @Pattern(regexp = "^(\\+33|0033|0)[1-9]([\\s.\\-]?[0-9]{2}){4}$", message = "Numéro de téléphone non valide")
    private String telephoneParent1;
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email non valide")
    private String emailParent2;
    @Pattern(regexp = "^(\\+33|0033|0)[1-9]([\\s.\\-]?[0-9]{2}){4}$", message = "Numéro de téléphone non valide")
    private String telephoneParent2;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String informationsMedicales;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String pai;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String informationsAlimentaires;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String traitementMatin;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String traitementMidi;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String traitementSoir;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String traitementSiBesoin;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String autresInformations;
    @Lob
    @Column(columnDefinition = "TEXT")
    private String aPrendreEnSortie;
}
