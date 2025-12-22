package com.tarnof.enjoyrestapi.dto;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SejourDTO {
    private int id;
    private String nom;
    private String description;
    private Date dateDebut;
    private Date dateFin;
    private String lieuDuSejour;
    private DirecteurInfos directeur;
    private List<ProfilUtilisateurDTO> equipe;

    @Data
    public static class DirecteurInfos {
        private String tokenId;
        private String nom;
        private String prenom;
    }
}
