package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEquipe;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Privilege;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class SejourVerificationService {

    private final SejourRepository sejourRepository;
    private final UtilisateurRepository utilisateurRepository;

    public SejourVerificationService(SejourRepository sejourRepository, UtilisateurRepository utilisateurRepository) {
        this.sejourRepository = sejourRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public Sejour verifierSejourExiste(int sejourId) {
        return sejourRepository
                .findById(sejourId)
                .orElseThrow(() -> new ResourceNotFoundException("Séjour non trouvé avec l'ID: " + sejourId));
    }

    public void verifierAppartenanceAuSejour(int sejourId, String utilisateurTokenId) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenId(utilisateurTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + utilisateurTokenId));
        
        if (utilisateur.getRole() == Role.ADMIN) {
            return;
        }
        
        Sejour sejour = verifierSejourExiste(sejourId);
        
        boolean estDirecteur = sejour.getDirecteur() != null && sejour.getDirecteur().getTokenId().equals(utilisateurTokenId);
        boolean estDansEquipe = sejour.getEquipeRoles() != null && sejour.getEquipeRoles().stream()
                .anyMatch(se -> se.getUtilisateur() != null && se.getUtilisateur().getTokenId().equals(utilisateurTokenId));
        
        if (!estDirecteur && !estDansEquipe) {
            throw new AccessDeniedException("Vous n'avez pas accès à ce séjour");
        }
    }

    public Sejour verifierSejourExisteEtAppartenance(int sejourId, String utilisateurTokenId) {
        verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        return verifierSejourExiste(sejourId);
    }

    /**
     * Réservé aux actions de gestion (privilège {@link Privilege#GESTION_SEJOURS} pour ce séjour précis).
     */
    public void verifierDroitGestionSejour(int sejourId, String utilisateurTokenId) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenId(utilisateurTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + utilisateurTokenId));
        if (utilisateur.getRole() == Role.ADMIN) {
            return;
        }
        Sejour sejour = verifierSejourExiste(sejourId);
        boolean estDirecteurDuSejour = sejour.getDirecteur() != null
                && sejour.getDirecteur().getTokenId().equals(utilisateurTokenId);
        boolean equipeAvecGestion = sejour.getEquipeRoles() != null
                && sejour.getEquipeRoles().stream()
                .filter(se -> se.getUtilisateur() != null && se.getUtilisateur().getTokenId().equals(utilisateurTokenId))
                .map(SejourEquipe::getRoleSejour)
                .anyMatch(role -> role != null && role.getPrivileges().contains(Privilege.GESTION_SEJOURS));
        if (!estDirecteurDuSejour && !equipeAvecGestion) {
            throw new AccessDeniedException("Vous n'avez pas les droits de gestion sur ce séjour");
        }
    }
}
