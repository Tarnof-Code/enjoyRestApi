package com.tarnof.enjoyrestapi.services.impl;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.SejourService;

import jakarta.transaction.Transactional;

@Service
public class SejourServiceImpl implements SejourService {

    @Autowired
    private SejourRepository sejourRepository;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public List<Sejour> getAllSejours() {
        return sejourRepository.findAll();
    }

    @Override
    public Sejour creerSejour(CreateSejourRequest request) {
        Utilisateur directeur = utilisateurRepository.findByTokenId(request.getDirecteurTokenId())
            .orElseThrow(() -> new RuntimeException("Directeur non trouvé avec l'ID: " + request.getDirecteurTokenId()));

        Sejour sejour = Sejour.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .lieuDuSejour(request.getLieuDuSejour())
                .directeur(directeur)
                .build();

        Objects.requireNonNull(sejour, "Séjour non créé");

        return sejourRepository.save(sejour);
    }

    @Override
    public Sejour modifierSejour(int id, CreateSejourRequest request) {
        Sejour sejourExistant = sejourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séjour non trouvé avec l'ID: " + id));
        Utilisateur directeur = utilisateurRepository.findByTokenId(request.getDirecteurTokenId())
            .orElseThrow(() -> new RuntimeException("Directeur non trouvé avec l'ID: " + request.getDirecteurTokenId()));    
        sejourExistant.setNom(request.getNom());
        sejourExistant.setDescription(request.getDescription());
        sejourExistant.setDateDebut(request.getDateDebut());
        sejourExistant.setDateFin(request.getDateFin());
        sejourExistant.setLieuDuSejour(request.getLieuDuSejour());
        sejourExistant.setDirecteur(directeur);
        return sejourRepository.save(sejourExistant);
    }

    @Override
    @Transactional
    public void supprimerSejour(int id) {
        Optional<Sejour> sejour = sejourRepository.findById(id);
        if (sejour.isPresent()) {
            sejourRepository.deleteById(id);
        } else {
            throw new RuntimeException("Séjour non trouvé avec l'ID: " + id);
        }
    }

    @Override
    public List<Sejour> getSejoursByDirecteur(String directeurTokenId) {
        Utilisateur directeur = utilisateurRepository.findByTokenId(directeurTokenId)
                .orElseThrow(() -> new RuntimeException("Directeur non trouvé avec le token ID: " + directeurTokenId));
        return sejourRepository.findByDirecteur(directeur);
    }
}
