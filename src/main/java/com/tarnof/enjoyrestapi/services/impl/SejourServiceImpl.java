package com.tarnof.enjoyrestapi.services.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.tarnof.enjoyrestapi.dto.ProfilUtilisateurDTO;
import com.tarnof.enjoyrestapi.dto.SejourDTO;
import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEquipe;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.RoleSejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.payload.request.MembreEquipeRequest;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import com.tarnof.enjoyrestapi.payload.response.AuthenticationResponse;
import com.tarnof.enjoyrestapi.repositories.RefreshTokenRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.SejourEquipeRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.entities.SejourEquipeId;
import com.tarnof.enjoyrestapi.services.AuthenticationService;
import com.tarnof.enjoyrestapi.services.SejourService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SejourServiceImpl implements SejourService {

    private final SejourRepository sejourRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final AuthenticationService authenticationService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SejourEquipeRepository sejourEquipeRepository;

    @Override
    public List<SejourDTO> getAllSejours() {
        return sejourRepository.findAll().stream()
                .map(sejour -> mapToDTO(sejour, false))
                .collect(Collectors.toList());
    }

    @Override
    public SejourDTO getSejourById(int id) {
        Sejour sejour = sejourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séjour non trouvé avec l'ID: " + id));
        return mapToDTO(sejour,true);
    }

    @Override
    public SejourDTO creerSejour(CreateSejourRequest request) {
        Utilisateur directeur = null;
        if (request.getDirecteurTokenId() != null && !request.getDirecteurTokenId().isBlank()) {
            directeur = utilisateurRepository.findByTokenId(request.getDirecteurTokenId())
                .orElseThrow(() -> new RuntimeException("Directeur non trouvé avec l'ID: " + request.getDirecteurTokenId()));
        }
        Sejour sejour = Sejour.builder()
                .nom(request.getNom())
                .description(request.getDescription())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .lieuDuSejour(request.getLieuDuSejour())
                .directeur(directeur)
                .build();
        Objects.requireNonNull(sejour, "Séjour non créé");
        Sejour savedSejour = sejourRepository.save(sejour);
        return mapToDTO(savedSejour, false);
    }

    @Override
    public SejourDTO modifierSejour(int id, CreateSejourRequest request) {
        Sejour sejourExistant = sejourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séjour non trouvé avec l'ID: " + id));      
        Utilisateur directeur = null;
        if (request.getDirecteurTokenId() != null && !request.getDirecteurTokenId().isBlank()) {
            directeur = utilisateurRepository.findByTokenId(request.getDirecteurTokenId())
                .orElseThrow(() -> new RuntimeException("Directeur non trouvé avec l'ID: " + request.getDirecteurTokenId()));
        }
        sejourExistant.setNom(request.getNom());
        sejourExistant.setDescription(request.getDescription());
        sejourExistant.setDateDebut(request.getDateDebut());
        sejourExistant.setDateFin(request.getDateFin());
        sejourExistant.setLieuDuSejour(request.getLieuDuSejour());
        sejourExistant.setDirecteur(directeur);
        Sejour savedSejour = sejourRepository.save(sejourExistant);
        return mapToDTO(savedSejour, false);
    }

    @Override
    @Transactional
    public void ajouterMembreEquipe(int sejourId, RegisterRequest registerRequest, MembreEquipeRequest membreEquipeRequest) {
        if (membreEquipeRequest == null && registerRequest == null) {
            throw new IllegalArgumentException("Une requête d'ajout ou d'inscription est requise");
        }
        Sejour sejour = sejourRepository.findById(sejourId)
                .orElseThrow(() -> new RuntimeException("Séjour non trouvé avec l'ID: " + sejourId));
        Utilisateur membreAAjouter;
        RoleSejour roleSejour;
        if (membreEquipeRequest != null) {
            membreAAjouter = traiterAjoutMembreExistant(membreEquipeRequest, sejour.getDateFin().toInstant());
            roleSejour = membreEquipeRequest.getRoleSejour();
        } else {
            membreAAjouter = traiterInscriptionNouveauMembre(registerRequest, sejour.getDateFin().toInstant());
            roleSejour = registerRequest.getRoleSejour();
        }
        if (sejour.getEquipeRoles() != null) {
            boolean dejaDansEquipe = sejour.getEquipeRoles().stream()
                    .anyMatch(se -> se.getUtilisateur().getId() == membreAAjouter.getId());
            if (dejaDansEquipe) {
                throw new ResourceAlreadyExistsException("Cet utilisateur fait déjà partie de l'équipe");
            }
        }
        if (sejour.getEquipeRoles() == null) {
            sejour.setEquipeRoles(new ArrayList<>());
        }
        SejourEquipe sejourEquipe = SejourEquipe.builder()
                .sejour(sejour)
                .utilisateur(membreAAjouter)
                .roleSejour(roleSejour)
                .build();
        sejour.getEquipeRoles().add(sejourEquipe);
        sejourRepository.save(sejour);
    }

    private Utilisateur traiterAjoutMembreExistant(MembreEquipeRequest request, Instant dateFinSejour) {
        Utilisateur membre = utilisateurRepository.findByTokenId(request.getTokenId())
                .orElseThrow(() -> new RuntimeException("Membre non trouvé avec l'ID: " + request.getTokenId()));
        if(membre.getSejoursEquipe() != null && !membre.getSejoursEquipe().isEmpty()) {
            Instant dateFinMax = membre.getSejoursEquipe().stream()
                    .map(SejourEquipe::getSejour)
                    .map(Sejour::getDateFin)
                    .filter(Objects::nonNull)
                    .map(Date::toInstant)
                    .max(Instant::compareTo)
                    .orElse(dateFinSejour); 
            Instant dateFinLaPlusEloignee = dateFinMax.isAfter(dateFinSejour) ? dateFinMax : dateFinSejour;        
            Instant nouvelleDateExpiration = dateFinLaPlusEloignee.plus(30, ChronoUnit.DAYS);        
            if (membre.getRefreshToken() != null) {
                RefreshToken refreshToken = membre.getRefreshToken();
                refreshToken.setExpiryDate(nouvelleDateExpiration);
                refreshTokenRepository.save(refreshToken);
            }
        } else {
            Instant nouvelleDateExpiration = dateFinSejour.plus(30, ChronoUnit.DAYS);            
            if (membre.getRefreshToken() != null) {
                RefreshToken refreshToken = membre.getRefreshToken();
                refreshToken.setExpiryDate(nouvelleDateExpiration);
                refreshTokenRepository.save(refreshToken);
            }
        }     
        return membre;
    }

    private Utilisateur traiterInscriptionNouveauMembre(RegisterRequest request, Instant dateFinSejour) {
        Instant dateExpiration = dateFinSejour.plus(30, ChronoUnit.DAYS);
        request.setDateExpiration(dateExpiration);
        if (request.getRole() == null) {
            request.setRole(Role.BASIC_USER);
        }
        AuthenticationResponse authResponse = authenticationService.register(request);
        return utilisateurRepository.findByTokenId(authResponse.getTokenId())
                .orElseThrow(() -> new RuntimeException("Erreur lors de la récupération du nouveau compte: " + request.getEmail()));
    }

    @Override
    @Transactional
    public void modifierRoleMembreEquipe(int sejourId, String membreTokenId, RoleSejour nouveauRole) {
        Sejour sejour = sejourRepository.findById(sejourId)
                .orElseThrow(() -> new RuntimeException("Séjour non trouvé avec l'ID: " + sejourId));
        Utilisateur membre = utilisateurRepository.findByTokenId(membreTokenId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé avec l'ID: " + membreTokenId));
        
        if (sejour.getEquipeRoles() == null || sejour.getEquipeRoles().isEmpty()) {
            throw new RuntimeException("Le membre ne fait pas partie de l'équipe de ce séjour");
        }
        
        SejourEquipe sejourEquipe = sejour.getEquipeRoles().stream()
                .filter(se -> se.getUtilisateur().getId() == membre.getId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Le membre ne fait pas partie de l'équipe de ce séjour"));
        
        sejourEquipe.setRoleSejour(nouveauRole);
        sejourRepository.save(sejour);
    }

    @Override
    @Transactional
    public void supprimerMembreEquipe(int sejourId, String membreTokenId) {
        Utilisateur membre = utilisateurRepository.findByTokenId(membreTokenId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé avec l'ID: " + membreTokenId));   
        SejourEquipeId sejourEquipeId = new SejourEquipeId(sejourId, membre.getId());
        if (!sejourEquipeRepository.existsById(sejourEquipeId)) {
            throw new RuntimeException("Le membre ne fait pas partie de l'équipe de ce séjour");
        }
        sejourEquipeRepository.deleteById(sejourEquipeId);
        sejourEquipeRepository.flush();     
        Utilisateur membreRecharge = utilisateurRepository.findByTokenId(membreTokenId)
                .orElseThrow(() -> new RuntimeException("Membre non trouvé avec l'ID: " + membreTokenId));       
        if (membreRecharge.getSejoursEquipe() != null && !membreRecharge.getSejoursEquipe().isEmpty()) {
            Instant dateFinMax = membreRecharge.getSejoursEquipe().stream()
                    .map(SejourEquipe::getSejour)
                    .map(Sejour::getDateFin)
                    .filter(Objects::nonNull)
                    .map(Date::toInstant)
                    .max(Instant::compareTo)
                    .orElse(null);           
            if (dateFinMax != null && membreRecharge.getRefreshToken() != null) {
                Instant nouvelleDateExpiration = dateFinMax.plus(30, ChronoUnit.DAYS);
                RefreshToken refreshToken = membreRecharge.getRefreshToken();
                refreshToken.setExpiryDate(nouvelleDateExpiration);
                refreshTokenRepository.save(refreshToken);
            }
        }
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
    public List<SejourDTO> getSejoursByDirecteur(String directeurTokenId) {
        Utilisateur directeur = utilisateurRepository.findByTokenId(directeurTokenId)
                .orElseThrow(() -> new RuntimeException("Directeur non trouvé avec le token ID: " + directeurTokenId));
        return sejourRepository.findByDirecteur(directeur).stream()
                .map(sejour -> mapToDTO(sejour,false))
                .collect(Collectors.toList());
    }

    private SejourDTO mapToDTO(Sejour sejour,Boolean includeEquipe) {
        SejourDTO dto = new SejourDTO();
        dto.setId(sejour.getId());
        dto.setNom(sejour.getNom());
        dto.setDescription(sejour.getDescription());
        dto.setDateDebut(sejour.getDateDebut());
        dto.setDateFin(sejour.getDateFin());
        dto.setLieuDuSejour(sejour.getLieuDuSejour());
        if (sejour.getDirecteur() != null) {
            SejourDTO.DirecteurInfos directeurInfos = new SejourDTO.DirecteurInfos();
            directeurInfos.setTokenId(sejour.getDirecteur().getTokenId());
            directeurInfos.setNom(sejour.getDirecteur().getNom());
            directeurInfos.setPrenom(sejour.getDirecteur().getPrenom());
            dto.setDirecteur(directeurInfos);
        }
        if (includeEquipe && sejour.getEquipeRoles() != null && !sejour.getEquipeRoles().isEmpty()) {
            List<ProfilUtilisateurDTO> equipeInfos = sejour.getEquipeRoles().stream()
                .map(sejourEquipe -> {
                    Utilisateur membre = sejourEquipe.getUtilisateur();
                    
                    ProfilUtilisateurDTO info = new ProfilUtilisateurDTO();
                    info.setTokenId(membre.getTokenId());
                    info.setRole(membre.getRole()); 
                    info.setRoleSejour(sejourEquipe.getRoleSejour());                   
                    info.setNom(membre.getNom());
                    info.setPrenom(membre.getPrenom());
                    info.setGenre(membre.getGenre());
                    info.setEmail(membre.getEmail());
                    info.setTelephone(membre.getTelephone());
                    info.setDateNaissance(membre.getDateNaissance());
                    info.setDateExpirationCompte(membre.getDateExpirationCompte());
                    return info;
                })
                .collect(Collectors.toList());
            dto.setEquipe(equipeInfos);
        }
        return dto;
    }
}

