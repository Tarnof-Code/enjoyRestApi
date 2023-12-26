package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.UtilisateurService;
import com.tarnof.enjoyrestapi.dto.ProfilUtilisateurDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        try{
            utilisateur.setMotDePasse(bCryptPasswordEncoder.encode(utilisateur.getMotDePasse()));
            return utilisateurRepository.save(utilisateur);
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    @Override
    public List<ProfilUtilisateurDTO> getAllUtilisateursDTO() {
        try{
            List<Utilisateur> listeUtilisateurs = utilisateurRepository.findAll();
            return listeUtilisateurs.stream()
                    .map(this::mapUtilisateurToProfilDTO)
                    .collect(Collectors.toList());
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Utilisateur> profilUtilisateur(String tokenId) {
        return utilisateurRepository.findByTokenId(tokenId);
    }

    @Override
    public ProfilUtilisateurDTO mapUtilisateurToProfilDTO(Utilisateur utilisateur) {
        ProfilUtilisateurDTO profilDTO = new ProfilUtilisateurDTO();
        profilDTO.setRole(utilisateur.getRole());
        profilDTO.setNom(utilisateur.getNom());
        profilDTO.setPrenom(utilisateur.getPrenom());
        profilDTO.setGenre(utilisateur.getGenre());
        profilDTO.setEmail(utilisateur.getEmail());
        profilDTO.setTelephone(utilisateur.getTelephone());
        profilDTO.setDateNaissance(utilisateur.getDateNaissance());
        profilDTO.setDateExpirationCompte(utilisateur.getDateExpirationCompte());
        return profilDTO;
    }

    @Override
    public Utilisateur modifierUtilisateur(Utilisateur utilisateur) {
        return null;
    }

    @Override
    public void supprimerUtilisateur(int id) {

    }


}
