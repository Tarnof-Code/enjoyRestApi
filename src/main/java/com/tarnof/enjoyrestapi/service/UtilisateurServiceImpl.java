package com.tarnof.enjoyrestapi.service;

import com.tarnof.enjoyrestapi.entity.Utilisateur;
import com.tarnof.enjoyrestapi.repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurServiceImpl implements UtilisateurService{
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        try{
            System.out.println("---------------Dans le service-----------");
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
    public Utilisateur modifierUtilisateur(Utilisateur utilisateur) {
        return null;
    }

    @Override
    public void supprimerUtilisateur(int id) {

    }
}
