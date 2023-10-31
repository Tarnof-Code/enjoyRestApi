package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.dto.ProfilUtilisateurDTO;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.services.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1/utilisateurs")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

    @GetMapping("/liste")
    public List<Utilisateur> consulterLaListeDesUtilisateurs(){
    //    System.out.println("+++++++++++++++Consultation des users++++++++++++++");
        return utilisateurService.getAllUtilisateurs();
    }

    @GetMapping("/profil")
    public ResponseEntity<ProfilUtilisateurDTO> profilUtilisateur(@RequestParam("email") String email) {
        Optional<Utilisateur> utilisateur = utilisateurService.profilUtilisateur(email);

        if (utilisateur.isPresent()) {
            // Mapper l'entité Utilisateur vers le DTO ProfilUtilisateurDTO
            ProfilUtilisateurDTO profilDTO = utilisateurService.mapUtilisateurToProfilDTO(utilisateur.get());
            return ResponseEntity.ok(profilDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }




}
