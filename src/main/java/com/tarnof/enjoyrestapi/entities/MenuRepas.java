package com.tarnof.enjoyrestapi.entities;

import com.tarnof.enjoyrestapi.enums.TypeRepas;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "menu_repas",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_menu_repas_sejour_date_type",
                        columnNames = {"sejour_id", "date_repas", "type_repas"}))
public class MenuRepas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Sejour sejour;

    @NotNull
    @Column(name = "date_repas", nullable = false)
    private LocalDate dateRepas;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type_repas", nullable = false, length = 32)
    private TypeRepas typeRepas;

    /**
     * Petit-déjeuner et goûter : description libre du repas. Pour déjeuner/dîner, utiliser les champs
     * structurés (entrée, plat, etc.) ; ce champ reste optionnel (notes complémentaires).
     */
    @Lob
    @Column(name = "detail_petit_dejeuner_ou_gouter", columnDefinition = "TEXT")
    private String detailPetitDejeunerOuGouter;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String entree;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String plat;

    /** Fromage, entremets ou service équivalent entre plat et dessert (terminologie collective). */
    @Lob
    @Column(name = "fromage_ou_entremet", columnDefinition = "TEXT")
    private String fromageOuEntremet;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String dessert;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "menu_repas_allergene",
            joinColumns = @JoinColumn(name = "menu_repas_id"),
            inverseJoinColumns = @JoinColumn(name = "reference_alimentaire_id"))
    private Set<ReferenceAlimentaire> allergenes = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "menu_repas_regime_preference",
            joinColumns = @JoinColumn(name = "menu_repas_id"),
            inverseJoinColumns = @JoinColumn(name = "reference_alimentaire_id"))
    private Set<ReferenceAlimentaire> regimesEtPreferences = new HashSet<>();

    public MenuRepas() {}

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

    public LocalDate getDateRepas() {
        return dateRepas;
    }

    public void setDateRepas(LocalDate dateRepas) {
        this.dateRepas = dateRepas;
    }

    public TypeRepas getTypeRepas() {
        return typeRepas;
    }

    public void setTypeRepas(TypeRepas typeRepas) {
        this.typeRepas = typeRepas;
    }

    public String getDetailPetitDejeunerOuGouter() {
        return detailPetitDejeunerOuGouter;
    }

    public void setDetailPetitDejeunerOuGouter(String detailPetitDejeunerOuGouter) {
        this.detailPetitDejeunerOuGouter = detailPetitDejeunerOuGouter;
    }

    public String getEntree() {
        return entree;
    }

    public void setEntree(String entree) {
        this.entree = entree;
    }

    public String getPlat() {
        return plat;
    }

    public void setPlat(String plat) {
        this.plat = plat;
    }

    public String getFromageOuEntremet() {
        return fromageOuEntremet;
    }

    public void setFromageOuEntremet(String fromageOuEntremet) {
        this.fromageOuEntremet = fromageOuEntremet;
    }

    public String getDessert() {
        return dessert;
    }

    public void setDessert(String dessert) {
        this.dessert = dessert;
    }

    public Set<ReferenceAlimentaire> getAllergenes() {
        return allergenes;
    }

    public void setAllergenes(Set<ReferenceAlimentaire> allergenes) {
        this.allergenes = allergenes;
    }

    public Set<ReferenceAlimentaire> getRegimesEtPreferences() {
        return regimesEtPreferences;
    }

    public void setRegimesEtPreferences(Set<ReferenceAlimentaire> regimesEtPreferences) {
        this.regimesEtPreferences = regimesEtPreferences;
    }
}
