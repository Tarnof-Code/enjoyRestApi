package com.tarnof.enjoyrestapi.controller;

import com.tarnof.enjoyrestapi.entity.Utilisateur;
import com.tarnof.enjoyrestapi.repository.UtilisateurRepository;
import com.tarnof.enjoyrestapi.service.UtilisateurService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UtilisateurController {
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private UtilisateurService utilisateurService;

    @PostMapping("/utilisateur")
    Utilisateur creerUtilisateur(@RequestBody Utilisateur nouvelUtilisateur) {
        System.out.println("+++++++++++++++Création d'un user++++++++++++++");
        return utilisateurService.creerUtilisateur(nouvelUtilisateur);
    }
    @GetMapping("/utilisateurs")
    public List<Utilisateur> consulterLaListeDesUtilisateurs(){
      //  System.out.println("+++++++++++++++Consultation des users++++++++++++++");
        return utilisateurService.getAllUtilisateurs();
    }

}
