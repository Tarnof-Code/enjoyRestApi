package com.tarnof.enjoyrestapi.dto;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.RoleSejour;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
public class ProfilUtilisateurDTO {
    private String tokenId;
    private Role role;
    private RoleSejour roleSejour;
    private String nom;
    private String prenom;
    private String genre;
    private String email;
    private String telephone;
    private Date dateNaissance;
    private Instant dateExpirationCompte;
}
