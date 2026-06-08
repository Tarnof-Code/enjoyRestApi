package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.payload.response.PhotoProfilContenu;
import com.tarnof.enjoyrestapi.payload.response.ProfilDto;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.handlers.ErrorResponse;
import com.tarnof.enjoyrestapi.payload.request.ChangePasswordRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateUserRequest;
import com.tarnof.enjoyrestapi.services.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/utilisateurs")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    public List<ProfilDto> consulterLaListeDesUtilisateurs() {
        List<ProfilDto> listeUtilisateursDTO = utilisateurService.getAllUtilisateursDTO();
        return listeUtilisateursDTO;
    }

    @GetMapping("/{role}")
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    public List<ProfilDto> consulterLaListeDesUtilisateursParRole(@PathVariable Role role) {
        List<ProfilDto> listeUtilisateursDTO = utilisateurService.getUtilisateursByRole(role);
        return listeUtilisateursDTO;
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('DIRECTION') or hasRole('ADMIN')")
    public ResponseEntity<?> chercherUtilisateurParEmail(@RequestParam("email") String email) {
        Optional<Utilisateur> utilisateur = utilisateurService.getUtilisateurByEmail(email);      
        if (utilisateur.isPresent()) {
            if(utilisateur.get().getRole().equals(Role.DIRECTION)  || utilisateur.get().getRole().equals(Role.ADMIN)) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .timestamp(Instant.now())
                        .message("Vous ne pouvez pas ajouter cette personne")
                        .path("/api/v1/utilisateurs/search")
                        .build();
                return ResponseEntity.badRequest().body(errorResponse);
            }

            ProfilDto profilDTO = utilisateurService.mapUtilisateurToProfilDTO(utilisateur.get());
            return ResponseEntity.ok(profilDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profil")
    public ResponseEntity<ProfilDto> profilUtilisateur(@RequestParam("tokenId") String tokenId) {
        Optional<Utilisateur> utilisateur = utilisateurService.profilUtilisateur(tokenId);
        if (utilisateur.isPresent()) {
            ProfilDto profilDTO = utilisateurService.mapUtilisateurToProfilDTO(utilisateur.get());
            return ResponseEntity.ok(profilDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{tokenId}")
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerUtilisateur(@PathVariable String tokenId) {
        utilisateurService.supprimerUtilisateur(tokenId);
    }

    @PutMapping
    public ResponseEntity<ProfilDto> modifierUtilisateur(@Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = utilisateurService.profilUtilisateur(request.tokenId())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec le token ID: " + request.tokenId()));

        List<String> droits = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Utilisateur currentUser = (Utilisateur) authentication.getPrincipal();
        boolean isAdmin = droits.contains("GESTION_UTILISATEURS");
        boolean isDirectorModifyingBasicUser = !isAdmin
                && currentUser.getRole() == Role.DIRECTION
                && utilisateur.getRole() == Role.BASIC_USER
                && !currentUser.getTokenId().equals(utilisateur.getTokenId());

        Utilisateur userUpdated;
        if (isAdmin) {
            userUpdated = utilisateurService.modifUserByAdmin(utilisateur, request);
        } else if (isDirectorModifyingBasicUser) {
            userUpdated = utilisateurService.modifUserByDirector(utilisateur, request);
        } else {
            userUpdated = utilisateurService.modifUserByUser(utilisateur, request);
        }

        ProfilDto profilDTO = utilisateurService.mapUtilisateurToProfilDTO(userUpdated);
        return ResponseEntity.ok(profilDTO);
    }

    @PatchMapping("/mot-de-passe")
    public ResponseEntity<?> changerMotDePasse(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        List<String> droits = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        boolean isAdmin = droits.contains("GESTION_UTILISATEURS");
        // Si l'utilisateur n'est pas admin, vérifier qu'il modifie son propre mot de passe
        if (!isAdmin) {
            Utilisateur currentUser = (Utilisateur) authentication.getPrincipal();
            if (!currentUser.getTokenId().equals(request.tokenId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                                .timestamp(Instant.now())
                                .message("Vous ne pouvez modifier que votre propre mot de passe")
                                .path("/api/v1/utilisateurs/mot-de-passe")
                                .build());
            }
            // L'utilisateur doit fournir son ancien mot de passe
            if (request.ancienMotDePasse() == null || request.ancienMotDePasse().isBlank()) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                .timestamp(Instant.now())
                                .message("L'ancien mot de passe est obligatoire")
                                .path("/api/v1/utilisateurs/mot-de-passe")
                                .build());
            }
            utilisateurService.changerMotDePasseParUtilisateur(
                    request.tokenId(),
                    request.ancienMotDePasse(),
                    request.nouveauMotDePasse()
            );
        } else {
            // L'admin peut changer sans l'ancien mot de passe
            utilisateurService.changerMotDePasseParAdmin(
                    request.tokenId(),
                    request.nouveauMotDePasse()
            );
        }
        return ResponseEntity.ok().body(Map.of("message", "Mot de passe modifié avec succès"));
    }

    @PostMapping(value = "/{tokenId}/photo-profil", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProfilDto> mettreAJourPhotoProfil(
            @PathVariable String tokenId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        Utilisateur currentUser = (Utilisateur) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("GESTION_UTILISATEURS"::equals);
        ProfilDto profil = utilisateurService.mettreAJourPhotoProfil(
                tokenId, file, currentUser.getTokenId(), isAdmin);
        return ResponseEntity.ok(profil);
    }

    @GetMapping("/{tokenId}/photo-profil")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ResponseEntity<InputStreamResource> chargerPhotoProfil(@PathVariable String tokenId) {
        PhotoProfilContenu photo = utilisateurService.chargerPhotoProfil(tokenId);
        InputStreamResource resource = new InputStreamResource(photo.contenu());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, photo.mimeType())
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(photo.taille()))
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(resource);
    }

    @DeleteMapping("/{tokenId}/photo-profil")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerPhotoProfil(
            @PathVariable String tokenId,
            Authentication authentication) {
        Utilisateur currentUser = (Utilisateur) authentication.getPrincipal();
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("GESTION_UTILISATEURS"::equals);
        utilisateurService.supprimerPhotoProfil(tokenId, currentUser.getTokenId(), isAdmin);
    }

}
