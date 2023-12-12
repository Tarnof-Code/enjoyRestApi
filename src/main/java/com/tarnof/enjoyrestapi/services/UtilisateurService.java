package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.dto.ProfilUtilisateurDTO;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public interface UtilisateurService {
    public Utilisateur creerUtilisateur(Utilisateur utilisateur);
    public List<ProfilUtilisateurDTO> getAllUtilisateursDTO();
    public Utilisateur modifierUtilisateur(Utilisateur utilisateur);
    public void supprimerUtilisateur(int id);
    public Optional<Utilisateur> profilUtilisateur(String email);
    ProfilUtilisateurDTO mapUtilisateurToProfilDTO(Utilisateur utilisateur);
}
