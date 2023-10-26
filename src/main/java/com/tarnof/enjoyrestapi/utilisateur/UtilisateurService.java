package com.tarnof.enjoyrestapi.utilisateur;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public interface UtilisateurService {
    public Utilisateur creerUtilisateur(Utilisateur utilisateur);
    public List<Utilisateur> getAllUtilisateurs();
    public Utilisateur modifierUtilisateur(Utilisateur utilisateur);
    public void supprimerUtilisateur(int id);
    public Optional<Utilisateur> profilUtilisateur(String email);
    ProfilUtilisateurDTO mapUtilisateurToProfilDTO(Utilisateur utilisateur);
}
