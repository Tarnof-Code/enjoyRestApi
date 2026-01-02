package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.exceptions.EmailDejaUtiliseException;
import com.tarnof.enjoyrestapi.exceptions.UtilisateurException;
import com.tarnof.enjoyrestapi.payload.request.UpdateUserRequest;
import com.tarnof.enjoyrestapi.repositories.RefreshTokenRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.UtilisateurService;

import jakarta.transaction.Transactional;

import com.tarnof.enjoyrestapi.dto.ProfilUtilisateurDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {
    private final UtilisateurRepository utilisateurRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SejourRepository sejourRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        if(utilisateurRepository.existsByEmail(utilisateur.getEmail()) || utilisateurRepository.existsByTelephone(utilisateur.getTelephone())){
            throw new EmailDejaUtiliseException("L'email ou le numéro de téléphone est déjà utilisé par un autre compte.");
        }
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
    public List<ProfilUtilisateurDTO> getUtilisateursByRole(Role role) {
        try{
            List<Utilisateur> listeUtilisateurs = utilisateurRepository.findByRole(role);
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
    public Optional<Utilisateur> getUtilisateurByEmail(String email) {
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
        profilDTO.setDateExpirationCompte(utilisateur.getDateExpirationCompte());
        profilDTO.setTokenId(utilisateur.getTokenId());
        return profilDTO;
    }

    @Override
    public Utilisateur modifUserByUser(Utilisateur utilisateur, UpdateUserRequest request) {
        System.out.println("+++++++++++++++++Dans modifUserByUser++++++++++++++++");
        return modifUser(utilisateur, request, false);
    }

    @Override
    public Utilisateur modifUserByAdmin(Utilisateur utilisateur, UpdateUserRequest request) {
        System.out.println("+++++++++++++++++Dans modifUserByAdmin++++++++++++++++");
        return modifUser(utilisateur, request, true);
    }

    private Utilisateur modifUser(Utilisateur utilisateur, UpdateUserRequest request, boolean isAdmin) {
        // Vérifier si l'email est déjà utilisé par un autre utilisateur
        if (!utilisateur.getEmail().equals(request.getEmail()) && utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new EmailDejaUtiliseException("L'email est déjà utilisé par un autre compte.");
        }
        Utilisateur.UtilisateurBuilder builder = utilisateur.toBuilder()
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .genre(request.getGenre())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .dateNaissance(request.getDateNaissance());

        if (isAdmin) {
              // Si le rôle change de DIRECTION vers autre chose, retirer le directeur des séjours
            if (request.getRole() != null && utilisateur.getRole() == Role.DIRECTION && request.getRole() != Role.DIRECTION) {
                List<Sejour> sejoursDiriges = sejourRepository.findByDirecteur(utilisateur);
                if (!sejoursDiriges.isEmpty()) {
                    sejoursDiriges.forEach(sejour -> sejour.setDirecteur(null));
                    sejourRepository.saveAll(sejoursDiriges);
                }
            }
            builder.role(request.getRole());
            if (utilisateur.getRefreshToken() != null){
                RefreshToken refreshToken = utilisateur.getRefreshToken();
                Instant nouvelleDateExpiration = request.getDateExpirationCompte();
                refreshToken.setExpiryDate(nouvelleDateExpiration);
                refreshTokenRepository.save(refreshToken);
            }
        }

        Utilisateur utilisateurModifie = builder.build();
        Objects.requireNonNull(utilisateurModifie, "L'utilisateur n'a pas pu être modifié");
        utilisateurRepository.save(utilisateurModifie);
        return utilisateurModifie;
    }



    @Override
    @Transactional
    public void supprimerUtilisateur(String tokenId) {
        Optional<Utilisateur> utilisateur = utilisateurRepository.findByTokenId(tokenId);
        if (utilisateur.isPresent()) {
            // La suppression en cascade gérera automatiquement les SejourEquipe liés
            // grâce à cascade = CascadeType.ALL dans l'entité Utilisateur
            utilisateurRepository.deleteByTokenId(tokenId);
        } else {
            throw new UtilisateurException("L'utilisateur n'existe pas");
        }
    }

    @Override
    public Utilisateur changerMotDePasseParAdmin(String tokenId, String nouveauMotDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new UtilisateurException("Utilisateur non trouvé"));
        utilisateur.setMotDePasse(bCryptPasswordEncoder.encode(nouveauMotDePasse));
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur changerMotDePasseParUtilisateur(String tokenId, String ancienMotDePasse, String nouveauMotDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenId(tokenId)
                .orElseThrow(() -> new UtilisateurException("Utilisateur non trouvé"));
        if (!bCryptPasswordEncoder.matches(ancienMotDePasse, utilisateur.getMotDePasse())) {
            throw new UtilisateurException("L'ancien mot de passe est incorrect");
        }
        utilisateur.setMotDePasse(bCryptPasswordEncoder.encode(nouveauMotDePasse));
        return utilisateurRepository.save(utilisateur);
    }

}
