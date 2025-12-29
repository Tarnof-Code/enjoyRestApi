package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.dto.ProfilUtilisateurDTO;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.handlers.ErrorResponse;
import com.tarnof.enjoyrestapi.payload.request.UpdateUserRequest;
import com.tarnof.enjoyrestapi.services.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/utilisateurs")
@RequiredArgsConstructor
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    public List<ProfilUtilisateurDTO> consulterLaListeDesUtilisateurs() {
        List<ProfilUtilisateurDTO> listeUtilisateursDTO = utilisateurService.getAllUtilisateursDTO();
        return listeUtilisateursDTO;
    }

    @GetMapping("/{role}")
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    public List<ProfilUtilisateurDTO> consulterLaListeDesUtilisateursParRole(@PathVariable Role role) {
        List<ProfilUtilisateurDTO> listeUtilisateursDTO = utilisateurService.getUtilisateursByRole(role);
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

            ProfilUtilisateurDTO profilDTO = utilisateurService.mapUtilisateurToProfilDTO(utilisateur.get());
            return ResponseEntity.ok(profilDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/profil")
    public ResponseEntity<ProfilUtilisateurDTO> profilUtilisateur(@RequestParam("tokenId") String tokenId) {
        Optional<Utilisateur> utilisateur = utilisateurService.profilUtilisateur(tokenId);
        if (utilisateur.isPresent()) {
            ProfilUtilisateurDTO profilDTO = utilisateurService.mapUtilisateurToProfilDTO(utilisateur.get());
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
    public ResponseEntity<ProfilUtilisateurDTO> modifierUtilisateur(@Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = utilisateurService.profilUtilisateur(request.getTokenId())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec le token ID: " + request.getTokenId()));

        List<String> droits = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Utilisateur userUpdated = droits.contains("GESTION_UTILISATEURS")
                ? utilisateurService.modifUserByAdmin(utilisateur, request)
                : utilisateurService.modifUserByUser(utilisateur, request);

        ProfilUtilisateurDTO profilDTO = utilisateurService.mapUtilisateurToProfilDTO(userUpdated);
        return ResponseEntity.ok(profilDTO);
    }

}
