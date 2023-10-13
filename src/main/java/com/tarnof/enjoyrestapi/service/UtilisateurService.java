package com.tarnof.enjoyrestapi.service;

import com.tarnof.enjoyrestapi.entity.Utilisateur;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public interface UtilisateurService {
    public Utilisateur creerUtilisateur(Utilisateur utilisateur);
    public List<Utilisateur> getAllUtilisateurs();
    public Utilisateur modifierUtilisateur(Utilisateur utilisateur);
    public void supprimerUtilisateur(int id);
}
