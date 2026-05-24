package com.tarnof.enjoyrestapi.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Compte rendu de réunion rattaché à un séjour.
 * Le champ {@link #contenuJson} sérialise le document TipTap (JSON ProseMirror).
 */
@Entity
@Table(name = "reunion")
public class Reunion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sejour_id", nullable = false)
    private Sejour sejour;

    /** Date de la réunion (jour civil). */
    @Column(name = "date_reunion", nullable = false)
    private LocalDate dateReunion;

    /** Ordre du jour (court), optionnel. */
    @Column(name = "ordre_du_jour", length = 500)
    private String ordreDuJour;

    /** Document TipTap sérialisé en JSON ({@code {"type":"doc","content":[...]}}). */
    @Column(name = "contenu_json", nullable = false, columnDefinition = "TEXT")
    private String contenuJson;

    public Reunion() {}

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

    public LocalDate getDateReunion() {
        return dateReunion;
    }

    public void setDateReunion(LocalDate dateReunion) {
        this.dateReunion = dateReunion;
    }

    public String getOrdreDuJour() {
        return ordreDuJour;
    }

    public void setOrdreDuJour(String ordreDuJour) {
        this.ordreDuJour = ordreDuJour;
    }

    public String getContenuJson() {
        return contenuJson;
    }

    public void setContenuJson(String contenuJson) {
        this.contenuJson = contenuJson;
    }
}
