package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.exceptions.EmailDejaUtiliseException;
import com.tarnof.enjoyrestapi.payload.request.UpdateUserRequest;
import com.tarnof.enjoyrestapi.repositories.RefreshTokenRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.UtilisateurService;
import com.tarnof.enjoyrestapi.dto.ProfilUtilisateurDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {
    @Autowired
    private UtilisateurRepository utilisateurRepository;
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
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
        profilDTO.setTokenId(utilisateur.getTokenId());
        return profilDTO;
    }

    @Override
    public Utilisateur modifUserByUser(Utilisateur utilisateur, UpdateUserRequest request) {
        System.out.println("Dans modifUserByUser  coucou++++++++++++++++");
        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!utilisateur.getEmail().equals(request.getEmail()) && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new EmailDejaUtiliseException("L'email est déjà utilisé par un autre compte.");
        }
         Utilisateur utilisateurModifie = utilisateur.toBuilder()
                 .prenom(request.getPrenom())
                 .nom(request.getNom())
                 .genre(request.getGenre())
                 .email(request.getEmail())
                 .telephone(request.getTelephone())
                 .dateNaissance(request.getDateNaissance())
                 .build();
         utilisateurRepository.save(utilisateurModifie);
        return utilisateurModifie;
    }

    @Override
    public Utilisateur modifUserByAdmin(Utilisateur utilisateur, UpdateUserRequest request) {
        System.out.println("Dans modifUserByAdmin  coucou++++++++++++++++");
        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!utilisateur.getEmail().equals(request.getEmail()) && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new EmailDejaUtiliseException("L'email est déjà utilisé par un autre compte.");
        }
        Utilisateur utilisateurModifie = utilisateur.toBuilder()
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .genre(request.getGenre())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .dateNaissance(request.getDateNaissance())
                .role(request.getRole())
                .build();

        if (utilisateur.getRefreshToken() != null){
            RefreshToken refreshToken = utilisateur.getRefreshToken();
            Instant nouvelleDateExpiration = request.getDateExpirationCompte();
            // Convertir Instant en LocalDateTime
            LocalDateTime localDateTime = LocalDateTime.ofInstant(nouvelleDateExpiration, ZoneId.systemDefault());

            // Formater LocalDateTime dans le format de la base de données
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
            String dateExpirationString = localDateTime.format(formatter);
            refreshToken.setExpiryDate(nouvelleDateExpiration);
            refreshTokenRepository.save(refreshToken);
        }
        utilisateurRepository.save(utilisateurModifie);
        return utilisateurModifie;
    }


    @Override
    public void supprimerUtilisateur(int id) {

    }


}
