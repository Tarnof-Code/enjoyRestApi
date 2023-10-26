package com.tarnof.enjoyrestapi.utilisateur;
import lombok.Data;

import java.util.Date;

@Data
public class ProfilUtilisateurDTO {
    private String nom;
    private String prenom;
    private String genre;
    private String email;
    private String telephone;
    private Date dateNaissance;

}
