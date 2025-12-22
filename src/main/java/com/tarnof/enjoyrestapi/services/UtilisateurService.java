package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.dto.ProfilUtilisateurDTO;
import com.tarnof.enjoyrestapi.payload.request.UpdateUserRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public interface UtilisateurService {
    public Utilisateur creerUtilisateur(Utilisateur utilisateur);
    public List<ProfilUtilisateurDTO> getAllUtilisateursDTO();
    public List<ProfilUtilisateurDTO> getUtilisateursByRole(Role role);
    public Utilisateur modifUserByUser(Utilisateur utilisateur, UpdateUserRequest request);
    public Utilisateur modifUserByAdmin(Utilisateur utilisateur, UpdateUserRequest request);
    public void supprimerUtilisateur(String tokenId);
    public Optional<Utilisateur> profilUtilisateur(String tokenId);
    public Optional<Utilisateur> getUtilisateurByEmail(String email);
    ProfilUtilisateurDTO mapUtilisateurToProfilDTO(Utilisateur utilisateur);
}
