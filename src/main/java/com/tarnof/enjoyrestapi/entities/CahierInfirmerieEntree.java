package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.TypeAppelInfirmerie;
import com.tarnof.enjoyrestapi.enums.TypeSoinInfirmerie;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "cahier_infirmerie_entree")
public class CahierInfirmerieEntree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Sejour sejour;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enfant_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Enfant enfant;

    /**
     * Compte auteur de la fiche (saisie initiale). Peut être nul si compte supprimé (rétention des données).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "createur_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private Utilisateur createur;

    @NotNull
    @Column(name = "date_heure", nullable = false)
    private Instant dateHeure;

    @NotBlank
    @Column(nullable = false, length = 4000)
    private String description;

    /** Ex. main, tête — optionnel si non localisable. */
    @Column(name = "localisation_corps", length = 255)
    private String localisationCorps;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "cahier_infirmerie_soin", joinColumns = @JoinColumn(name = "entree_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "type_soin", nullable = false)
    private Set<TypeSoinInfirmerie> soins = new LinkedHashSet<>();

    @Column(name = "soins_autre_precision", length = 500)
    private String soinsAutrePrecision;

    /** Mesure en °C lorsque {@link TypeSoinInfirmerie#PRISE_TEMPERATURE} fait partie des soins ; sinon {@code null}. */
    @Column(name = "temperature_celsius", precision = 5, scale = 2)
    private BigDecimal temperatureCelsius;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "cahier_infirmerie_appel", joinColumns = @JoinColumn(name = "entree_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "type_appel", nullable = false)
    private Set<TypeAppelInfirmerie> appels = new LinkedHashSet<>();

    @Column(name = "appel_autre_precision", length = 500)
    private String appelAutrePrecision;

    /**
     * Personne de l'équipe du séjour ayant effectué les soins (directeur ou membre inscrit dans {@code sejour_equipe}).
     */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "soigneur_id", nullable = false)
    private Utilisateur soigneur;

    public CahierInfirmerieEntree() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    public Enfant getEnfant() {
        return enfant;
    }

    public void setEnfant(Enfant enfant) {
        this.enfant = enfant;
    }

    public Utilisateur getCreateur() {
        return createur;
    }

    public void setCreateur(Utilisateur createur) {
        this.createur = createur;
    }

    public Instant getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(Instant dateHeure) {
        this.dateHeure = dateHeure;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocalisationCorps() {
        return localisationCorps;
    }

    public void setLocalisationCorps(String localisationCorps) {
        this.localisationCorps = localisationCorps;
    }

    public Set<TypeSoinInfirmerie> getSoins() {
        return soins;
    }

    public void setSoins(Set<TypeSoinInfirmerie> soins) {
        this.soins = soins;
    }

    public String getSoinsAutrePrecision() {
        return soinsAutrePrecision;
    }

    public void setSoinsAutrePrecision(String soinsAutrePrecision) {
        this.soinsAutrePrecision = soinsAutrePrecision;
    }

    public BigDecimal getTemperatureCelsius() {
        return temperatureCelsius;
    }

    public void setTemperatureCelsius(BigDecimal temperatureCelsius) {
        this.temperatureCelsius = temperatureCelsius;
    }

    public Set<TypeAppelInfirmerie> getAppels() {
        return appels;
    }

    public void setAppels(Set<TypeAppelInfirmerie> appels) {
        this.appels = appels;
    }

    public String getAppelAutrePrecision() {
        return appelAutrePrecision;
    }

    public void setAppelAutrePrecision(String appelAutrePrecision) {
        this.appelAutrePrecision = appelAutrePrecision;
    }

    public Utilisateur getSoigneur() {
        return soigneur;
    }

    public void setSoigneur(Utilisateur soigneur) {
        this.soigneur = soigneur;
    }
}
