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
    public List<Utilisateur> getAllUtilisateurs() {
        try{
         //   System.out.println(utilisateurRepository.findAll());
            return (List<Utilisateur>) utilisateurRepository.findAll();
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Optional<Utilisateur> profilUtilisateur(String email) {
        return utilisateurRepository.findByEmail(email);
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
