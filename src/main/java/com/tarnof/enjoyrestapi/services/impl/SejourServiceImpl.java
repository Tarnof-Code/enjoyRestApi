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

import com.tarnof.enjoyrestapi.payload.response.ProfilDto;
import com.tarnof.enjoyrestapi.payload.response.SejourDto;
import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEquipe;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.RoleSejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
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
    public List<SejourDto> getAllSejours() {
        return sejourRepository.findAll().stream()
                .map(sejour -> mapToDTO(sejour, false))
                .collect(Collectors.toList());
    }

    @Override
    public SejourDto getSejourById(int id) {
        Sejour sejour = sejourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + id));
        return mapToDTO(sejour,true);
    }

    @Override
    public SejourDto creerSejour(CreateSejourRequest request) {
        Utilisateur directeur = null;
        if (request.directeurTokenId() != null && !request.directeurTokenId().isBlank()) {
            directeur = utilisateurRepository.findByTokenId(request.directeurTokenId())
                .orElseThrow(() -> new ResourceNotFoundException("Directeur non trouvé avec l'ID: " + request.directeurTokenId()));
        }
        Sejour sejour = Sejour.builder()
                .nom(request.nom())
                .description(request.description())
                .dateDebut(request.dateDebut())
                .dateFin(request.dateFin())
                .lieuDuSejour(request.lieuDuSejour())
                .directeur(directeur)
                .build();
        Objects.requireNonNull(sejour, "Séjour non créé");
        Sejour savedSejour = sejourRepository.save(sejour);
        return mapToDTO(savedSejour, false);
    }

    @Override
    public SejourDto modifierSejour(int id, CreateSejourRequest request) {
        Sejour sejourExistant = sejourRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + id));      
        Utilisateur directeur = null;
        if (request.directeurTokenId() != null && !request.directeurTokenId().isBlank()) {
            directeur = utilisateurRepository.findByTokenId(request.directeurTokenId())
                .orElseThrow(() -> new ResourceNotFoundException("Directeur non trouvé avec l'ID: " + request.directeurTokenId()));
        }
        sejourExistant.setNom(request.nom());
        sejourExistant.setDescription(request.description());
        sejourExistant.setDateDebut(request.dateDebut());
        sejourExistant.setDateFin(request.dateFin());
        sejourExistant.setLieuDuSejour(request.lieuDuSejour());
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
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
        Utilisateur membreAAjouter;
        RoleSejour roleSejour;
        if (membreEquipeRequest != null) {
            membreAAjouter = traiterAjoutMembreExistant(membreEquipeRequest, sejour.getDateFin().toInstant());
            roleSejour = membreEquipeRequest.roleSejour();
        } else {
            membreAAjouter = traiterInscriptionNouveauMembre(registerRequest, sejour.getDateFin().toInstant());
            roleSejour = registerRequest.roleSejour();
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
        Utilisateur membre = utilisateurRepository.findByTokenId(request.tokenId())
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé avec l'ID: " + request.tokenId()));
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
        Role role = request.role() != null ? request.role() : Role.BASIC_USER;
        RegisterRequest requestWithExpiration = new RegisterRequest(
            request.prenom(),
            request.nom(),
            request.genre(),
            request.dateNaissance(),
            request.telephone(),
            request.email(),
            request.motDePasse(),
            dateExpiration,
            role,
            request.roleSejour()
        );
        AuthenticationResponse authResponse = authenticationService.register(requestWithExpiration);
        return utilisateurRepository.findByTokenId(authResponse.tokenId())
                .orElseThrow(() -> new ResourceNotFoundException("Erreur lors de la récupération du nouveau compte: " + request.email()));
    }

    @Override
    @Transactional
    public void modifierRoleMembreEquipe(int sejourId, String membreTokenId, RoleSejour nouveauRole) {
        Sejour sejour = sejourRepository.findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
        Utilisateur membre = utilisateurRepository.findByTokenId(membreTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé avec l'ID: " + membreTokenId));
        
        if (sejour.getEquipeRoles() == null || sejour.getEquipeRoles().isEmpty()) {
            throw new ResourceNotFoundException("Le membre ne fait pas partie de l'équipe de ce séjour");
        }
        
        SejourEquipe sejourEquipe = sejour.getEquipeRoles().stream()
                .filter(se -> se.getUtilisateur().getId() == membre.getId())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Le membre ne fait pas partie de l'équipe de ce séjour"));
        
        sejourEquipe.setRoleSejour(nouveauRole);
        sejourRepository.save(sejour);
    }

    @Override
    @Transactional
    public void supprimerMembreEquipe(int sejourId, String membreTokenId) {
        Utilisateur membre = utilisateurRepository.findByTokenId(membreTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé avec l'ID: " + membreTokenId));   
        SejourEquipeId sejourEquipeId = new SejourEquipeId(sejourId, membre.getId());
        if (!sejourEquipeRepository.existsById(sejourEquipeId)) {
            throw new ResourceNotFoundException("Le membre ne fait pas partie de l'équipe de ce séjour");
        }
        sejourEquipeRepository.deleteById(sejourEquipeId);
        sejourEquipeRepository.flush();     
        Utilisateur membreRecharge = utilisateurRepository.findByTokenId(membreTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Membre non trouvé avec l'ID: " + membreTokenId));       
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
            throw new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + id);
        }
    }

    @Override
    public List<SejourDto> getSejoursByDirecteur(String directeurTokenId) {
        Utilisateur directeur = utilisateurRepository.findByTokenId(directeurTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Directeur non trouvé avec le token ID: " + directeurTokenId));
        return sejourRepository.findByDirecteur(directeur).stream()
                .map(sejour -> mapToDTO(sejour,false))
                .collect(Collectors.toList());
    }

    private SejourDto mapToDTO(Sejour sejour,Boolean includeEquipe) {
        SejourDto.DirecteurInfos directeurInfos = null;
        if (sejour.getDirecteur() != null) {
            directeurInfos = new SejourDto.DirecteurInfos(
                sejour.getDirecteur().getTokenId(),
                sejour.getDirecteur().getNom(),
                sejour.getDirecteur().getPrenom()
            );
        }
        
        List<ProfilDto> equipeInfos = null;
        if (includeEquipe && sejour.getEquipeRoles() != null && !sejour.getEquipeRoles().isEmpty()) {
            equipeInfos = sejour.getEquipeRoles().stream()
                .map(sejourEquipe -> {
                    Utilisateur membre = sejourEquipe.getUtilisateur();
                    return new ProfilDto(
                        membre.getTokenId(),
                        membre.getRole(),
                        sejourEquipe.getRoleSejour(),
                        membre.getNom(),
                        membre.getPrenom(),
                        membre.getGenre(),
                        membre.getEmail(),
                        membre.getTelephone(),
                        membre.getDateNaissance(),
                        membre.getDateExpirationCompte()
                    );
                })
                .collect(Collectors.toList());
        }
        
        return new SejourDto(
            sejour.getId(),
            sejour.getNom(),
            sejour.getDescription(),
            sejour.getDateDebut(),
            sejour.getDateFin(),
            sejour.getLieuDuSejour(),
            directeurInfos,
            equipeInfos
        );
    }
}

