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

@Entity
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

    public DossierEnfant() {
    }

    public DossierEnfant(Integer id, Enfant enfant, String emailParent1, String telephoneParent1, String emailParent2,
                         String telephoneParent2, String informationsMedicales, String pai, String informationsAlimentaires,
                         String traitementMatin, String traitementMidi, String traitementSoir, String traitementSiBesoin,
                         String autresInformations, String aPrendreEnSortie) {
        this.id = id;
        this.enfant = enfant;
        this.emailParent1 = emailParent1;
        this.telephoneParent1 = telephoneParent1;
        this.emailParent2 = emailParent2;
        this.telephoneParent2 = telephoneParent2;
        this.informationsMedicales = informationsMedicales;
        this.pai = pai;
        this.informationsAlimentaires = informationsAlimentaires;
        this.traitementMatin = traitementMatin;
        this.traitementMidi = traitementMidi;
        this.traitementSoir = traitementSoir;
        this.traitementSiBesoin = traitementSiBesoin;
        this.autresInformations = autresInformations;
        this.aPrendreEnSortie = aPrendreEnSortie;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Enfant getEnfant() {
        return enfant;
    }

    public void setEnfant(Enfant enfant) {
        this.enfant = enfant;
    }

    public String getEmailParent1() {
        return emailParent1;
    }

    public void setEmailParent1(String emailParent1) {
        this.emailParent1 = emailParent1;
    }

    public String getTelephoneParent1() {
        return telephoneParent1;
    }

    public void setTelephoneParent1(String telephoneParent1) {
        this.telephoneParent1 = telephoneParent1;
    }

    public String getEmailParent2() {
        return emailParent2;
    }

    public void setEmailParent2(String emailParent2) {
        this.emailParent2 = emailParent2;
    }

    public String getTelephoneParent2() {
        return telephoneParent2;
    }

    public void setTelephoneParent2(String telephoneParent2) {
        this.telephoneParent2 = telephoneParent2;
    }

    public String getInformationsMedicales() {
        return informationsMedicales;
    }

    public void setInformationsMedicales(String informationsMedicales) {
        this.informationsMedicales = informationsMedicales;
    }

    public String getPai() {
        return pai;
    }

    public void setPai(String pai) {
        this.pai = pai;
    }

    public String getInformationsAlimentaires() {
        return informationsAlimentaires;
    }

    public void setInformationsAlimentaires(String informationsAlimentaires) {
        this.informationsAlimentaires = informationsAlimentaires;
    }

    public String getTraitementMatin() {
        return traitementMatin;
    }

    public void setTraitementMatin(String traitementMatin) {
        this.traitementMatin = traitementMatin;
    }

    public String getTraitementMidi() {
        return traitementMidi;
    }

    public void setTraitementMidi(String traitementMidi) {
        this.traitementMidi = traitementMidi;
    }

    public String getTraitementSoir() {
        return traitementSoir;
    }

    public void setTraitementSoir(String traitementSoir) {
        this.traitementSoir = traitementSoir;
    }

    public String getTraitementSiBesoin() {
        return traitementSiBesoin;
    }

    public void setTraitementSiBesoin(String traitementSiBesoin) {
        this.traitementSiBesoin = traitementSiBesoin;
    }

    public String getAutresInformations() {
        return autresInformations;
    }

    public void setAutresInformations(String autresInformations) {
        this.autresInformations = autresInformations;
    }

    public String getAPrendreEnSortie() {
        return aPrendreEnSortie;
    }

    public void setAPrendreEnSortie(String aPrendreEnSortie) {
        this.aPrendreEnSortie = aPrendreEnSortie;
    }

    @Override
    public String toString() {
        return "DossierEnfant{" +
                "id=" + id +
                ", enfant=" + enfant +
                ", emailParent1='" + emailParent1 + '\'' +
                ", telephoneParent1='" + telephoneParent1 + '\'' +
                ", emailParent2='" + emailParent2 + '\'' +
                ", telephoneParent2='" + telephoneParent2 + '\'' +
                ", informationsMedicales='" + informationsMedicales + '\'' +
                ", pai='" + pai + '\'' +
                ", informationsAlimentaires='" + informationsAlimentaires + '\'' +
                ", traitementMatin='" + traitementMatin + '\'' +
                ", traitementMidi='" + traitementMidi + '\'' +
                ", traitementSoir='" + traitementSoir + '\'' +
                ", traitementSiBesoin='" + traitementSiBesoin + '\'' +
                ", autresInformations='" + autresInformations + '\'' +
                ", aPrendreEnSortie='" + aPrendreEnSortie + '\'' +
                '}';
    }
}
