package com.tarnof.enjoyrestapi.utilisateur;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/utilisateur")
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;

/*    @PostMapping("/utilisateur")
    Utilisateur creerUtilisateur(@RequestBody Utilisateur nouvelUtilisateur) {
        System.out.println("+++++++++++++++Création d'un user++++++++++++++");
        return utilisateurService.creerUtilisateur(nouvelUtilisateur);
    }
*/
    @CrossOrigin
    @GetMapping("/liste")
    public List<Utilisateur> consulterLaListeDesUtilisateurs(){
      //  System.out.println("+++++++++++++++Consultation des users++++++++++++++");
        return utilisateurService.getAllUtilisateurs();
    }

}
