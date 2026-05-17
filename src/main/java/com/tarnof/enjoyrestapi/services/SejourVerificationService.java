package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.entities.Activite;
import com.tarnof.enjoyrestapi.entities.CahierInfirmerieEntree;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.entities.SejourEquipe;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Privilege;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.repositories.SejourEquipeRepository;
import com.tarnof.enjoyrestapi.repositories.SejourRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class SejourVerificationService {

    private final SejourRepository sejourRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final SejourEquipeRepository sejourEquipeRepository;

    public SejourVerificationService(
            SejourRepository sejourRepository,
            UtilisateurRepository utilisateurRepository,
            SejourEquipeRepository sejourEquipeRepository) {
        this.sejourRepository = sejourRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.sejourEquipeRepository = sejourEquipeRepository;
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

    /**
     * Directeur du séjour ou ligne {@code sejour_equipe} pour ce séjour (même règle que les animateurs d'activité).
     * <p>Pas de court-circuit {@link Role#ADMIN} : l'utilisateur désigné doit réellement être rattaché au séjour
     * (ex. soigneur, membre d'une activité).</p>
     *
     * @throws IllegalArgumentException si le token est vide ou si l'utilisateur n'est ni directeur ni membre d'équipe
     */
    public Utilisateur exigerUtilisateurDirecteurOuMembreEquipeDuSejour(int sejourId, String utilisateurTokenId) {
        if (utilisateurTokenId == null || utilisateurTokenId.trim().isEmpty()) {
            throw new IllegalArgumentException("L'utilisateur est obligatoire.");
        }
        String token = utilisateurTokenId.trim();
        Utilisateur utilisateur =
                utilisateurRepository
                        .findByTokenId(token)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + token));
        Sejour sejour = verifierSejourExiste(sejourId);
        Utilisateur directeur = sejour.getDirecteur();
        boolean estDirecteurDuSejour = directeur != null && directeur.getId() == utilisateur.getId();
        if (!estDirecteurDuSejour
                && !sejourEquipeRepository.existsBySejour_IdAndUtilisateur_Id(sejourId, utilisateur.getId())) {
            throw new IllegalArgumentException(
                    "Cet utilisateur doit être le directeur du séjour ou un membre inscrit dans l'équipe de ce séjour.");
        }
        return utilisateur;
    }

    public Sejour verifierSejourExisteEtAppartenance(int sejourId, String utilisateurTokenId) {
        verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        return verifierSejourExiste(sejourId);
    }

    /**
     * Directeur du séjour, adjoint (rôle équipe avec {@link Privilege#GESTION_SEJOURS}), ou compte admin.
     */
    public boolean aDroitGestionCompleteSurSejour(int sejourId, String utilisateurTokenId) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenId(utilisateurTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + utilisateurTokenId));
        if (utilisateur.getRole() == Role.ADMIN) {
            return true;
        }
        Sejour sejour = verifierSejourExiste(sejourId);
        boolean estDirecteurDuSejour = sejour.getDirecteur() != null
                && sejour.getDirecteur().getTokenId().equals(utilisateurTokenId);
        boolean equipeAvecGestion = sejour.getEquipeRoles() != null
                && sejour.getEquipeRoles().stream()
                .filter(se -> se.getUtilisateur() != null && se.getUtilisateur().getTokenId().equals(utilisateurTokenId))
                .map(SejourEquipe::getRoleSejour)
                .anyMatch(role -> role != null && role.getPrivileges().contains(Privilege.GESTION_SEJOURS));
        return estDirecteurDuSejour || equipeAvecGestion;
    }

    /**
     * Réservé aux actions de gestion (privilège {@link Privilege#GESTION_SEJOURS} pour ce séjour précis).
     */
    public void verifierDroitGestionSejour(int sejourId, String utilisateurTokenId) {
        if (!aDroitGestionCompleteSurSejour(sejourId, utilisateurTokenId)) {
            throw new AccessDeniedException("Vous n'avez pas les droits de gestion sur ce séjour");
        }
    }

    /**
     * Modification ou suppression d'activité : direction / adjoint, ou animateur affecté à cette activité.
     */
    public void verifierDroitModificationOuSuppressionActivite(int sejourId, Activite activite, String utilisateurTokenId) {
        Utilisateur utilisateur = utilisateurRepository.findByTokenId(utilisateurTokenId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + utilisateurTokenId));
        if (utilisateur.getRole() == Role.ADMIN) {
            return;
        }
        if (aDroitGestionCompleteSurSejour(sejourId, utilisateurTokenId)) {
            return;
        }
        verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        boolean affecte = activite.getMembres() != null
                && activite.getMembres().stream()
                .filter(m -> m != null)
                .anyMatch(m -> utilisateurTokenId.equals(m.getTokenId()));
        if (!affecte) {
            throw new AccessDeniedException(
                    "Vous ne pouvez modifier ou supprimer que les activités auxquelles vous êtes affecté.");
        }
    }

    /**
     * Entrée du cahier d'infirmerie : gestion complète du séjour sur ce séjour ({@link #aDroitGestionCompleteSurSejour}),
     * {@link Role#ADMIN}, ou bien auteur ({@code createur}) ou personne désignée comme soigneur ({@code soigneur}).
     *
     * <p>Si {@code createur} est {@code null} (compte supprimé), la gestion complète / le soigneur / un admin peut modifier.
     */
    public void verifierDroitModificationEntreeCahierInfirmerie(
            int sejourId, CahierInfirmerieEntree entree, String utilisateurTokenId) {
        verifierDroitMutationEntreeCahierInfirmerie(
                sejourId,
                entree,
                utilisateurTokenId,
                "Vous ne pouvez modifier cette entrée que si vous en êtes l'auteur, le soigneur désigné, "
                        + "ou si vous avez les droits de gestion du séjour.");
    }

    /**
     * Suppression d'une entrée du cahier d'infirmerie : même périmètre que {@link #verifierDroitModificationEntreeCahierInfirmerie}.
     */
    public void verifierDroitSuppressionEntreeCahierInfirmerie(
            int sejourId, CahierInfirmerieEntree entree, String utilisateurTokenId) {
        verifierDroitMutationEntreeCahierInfirmerie(
                sejourId,
                entree,
                utilisateurTokenId,
                "Vous ne pouvez supprimer cette entrée que si vous en êtes l'auteur, le soigneur désigné, "
                        + "ou si vous avez les droits de gestion du séjour.");
    }

    private void verifierDroitMutationEntreeCahierInfirmerie(
            int sejourId,
            CahierInfirmerieEntree entree,
            String utilisateurTokenId,
            String messageSiRefus) {
        Utilisateur utilisateur =
                utilisateurRepository
                        .findByTokenId(utilisateurTokenId)
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Utilisateur non trouvé avec le token ID: " + utilisateurTokenId));
        if (utilisateur.getRole() == Role.ADMIN) {
            return;
        }
        if (aDroitGestionCompleteSurSejour(sejourId, utilisateurTokenId)) {
            return;
        }
        verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Utilisateur createur = entree.getCreateur();
        if (createur != null && utilisateurTokenId.equals(createur.getTokenId())) {
            return;
        }
        Utilisateur soigneur = entree.getSoigneur();
        if (soigneur != null && utilisateurTokenId.equals(soigneur.getTokenId())) {
            return;
        }
        throw new AccessDeniedException(messageSiRefus);
    }
}
