package com.tarnof.enjoyrestapi.dto;

import lombok.Data;

import java.util.Date;

@Data
public class SejourDTO {
    private int id;
    private String nom;
    private String description;
    private Date dateDebut;
    private Date dateFin;
    private String lieuDuSejour;
    private DirecteurInfos directeur;

    @Data
    public static class DirecteurInfos {
        private String tokenId;
        private String nom;
        private String prenom;
    }
}
