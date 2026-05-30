package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.GenreChambre;
import com.tarnof.enjoyrestapi.enums.TypeChambre;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "chambre",
        uniqueConstraints =
                @UniqueConstraint(name = "uk_chambre_sejour_identifiant", columnNames = {"sejour_id", "identifiant"}))
public class Chambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Le type de chambre est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "type_chambre", nullable = false)
    private TypeChambre typeChambre;

    /**
     * Identifiant officiel de la chambre dans le centre (numéro, lettre, mot…), unique par séjour.
     */
    @NotBlank(message = "L'identifiant de la chambre est obligatoire")
    @Column(nullable = false)
    private String identifiant;

    /** Nom informel optionnel donné par les enfants ou l'équipe. */
    private String nom;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Positive(message = "La capacité maximale doit être strictement positive")
    @Column(name = "capacite_max", nullable = false)
    private Integer capaciteMax;

    @NotNull(message = "Le genre autorisé est obligatoire")
    @Enumerated(EnumType.STRING)
    @Column(name = "genre_autorise", nullable = false)
    private GenreChambre genreAutorise;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String batiment;

    private String couloir;

    private Integer etage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    /**
     * Groupe dont les enfants sont autorisés à dormir dans cette chambre.
     * Réservé aux chambres {@link TypeChambre#ENFANT} ; si renseigné, seuls ses membres peuvent y être affectés.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupe_id")
    private Groupe groupe;

    /** Réservé aux chambres {@link TypeChambre#ENFANT}. */
    @ManyToMany
    @JoinTable(
            name = "chambre_referent",
            joinColumns = @JoinColumn(name = "chambre_id"),
            inverseJoinColumns = @JoinColumn(name = "utilisateur_id"))
    private List<Utilisateur> referents = new ArrayList<>();

    @OneToMany(mappedBy = "chambre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChambreOccupant> occupants = new ArrayList<>();

    public Chambre() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public TypeChambre getTypeChambre() {
        return typeChambre;
    }

    public void setTypeChambre(TypeChambre typeChambre) {
        this.typeChambre = typeChambre;
    }

    public String getIdentifiant() {
        return identifiant;
    }

    public void setIdentifiant(String identifiant) {
        this.identifiant = identifiant;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public Integer getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(Integer capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public GenreChambre getGenreAutorise() {
        return genreAutorise;
    }

    public void setGenreAutorise(GenreChambre genreAutorise) {
        this.genreAutorise = genreAutorise;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBatiment() {
        return batiment;
    }

    public void setBatiment(String batiment) {
        this.batiment = batiment;
    }

    public String getCouloir() {
        return couloir;
    }

    public void setCouloir(String couloir) {
        this.couloir = couloir;
    }

    public Integer getEtage() {
        return etage;
    }

    public void setEtage(Integer etage) {
        this.etage = etage;
    }

    public Sejour getSejour() {
        return sejour;
    }

    public void setSejour(Sejour sejour) {
        this.sejour = sejour;
    }

    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }

    public List<Utilisateur> getReferents() {
        return referents;
    }

    public void setReferents(List<Utilisateur> referents) {
        this.referents = referents != null ? referents : new ArrayList<>();
    }

    public List<ChambreOccupant> getOccupants() {
        return occupants;
    }

    public void setOccupants(List<ChambreOccupant> occupants) {
        this.occupants = occupants != null ? occupants : new ArrayList<>();
    }

    /** Sans {@code sejour} : évite lazy load et cycle avec {@link Sejour}. */
    @Override
    public String toString() {
        return "Chambre{" +
                "id=" + id +
                ", typeChambre=" + typeChambre +
                ", identifiant='" + identifiant + '\'' +
                ", nom='" + nom + '\'' +
                ", capaciteMax=" + capaciteMax +
                ", genreAutorise=" + genreAutorise +
                ", batiment='" + batiment + '\'' +
                ", couloir='" + couloir + '\'' +
                ", etage=" + etage +
                '}';
    }
}
