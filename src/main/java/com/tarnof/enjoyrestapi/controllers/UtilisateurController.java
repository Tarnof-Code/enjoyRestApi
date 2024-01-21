package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.dto.ProfilUtilisateurDTO;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.exceptions.EmailDejaUtiliseException;
import com.tarnof.enjoyrestapi.handlers.ErrorResponse;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateUserRequest;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.UtilisateurService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
public class UtilisateurController {

    @Autowired
    private UtilisateurService utilisateurService;
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @GetMapping("/liste")
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    public List<ProfilUtilisateurDTO> consulterLaListeDesUtilisateurs(){
        List<ProfilUtilisateurDTO> listeUtilisateursDTO = utilisateurService.getAllUtilisateursDTO();
        return listeUtilisateursDTO;
    }

    @GetMapping("/profil")
    public ResponseEntity<ProfilUtilisateurDTO> profilUtilisateur(@RequestParam("tokenId") String tokenId) {
        Optional<Utilisateur> utilisateur = utilisateurService.profilUtilisateur(tokenId);
        if (utilisateur.isPresent()) {
            // Mapper l'entité Utilisateur vers le DTO ProfilUtilisateurDTO
            ProfilUtilisateurDTO profilDTO = utilisateurService.mapUtilisateurToProfilDTO(utilisateur.get());
            return ResponseEntity.ok(profilDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/modifierInfos")
    public ResponseEntity<?> modifierUtilisateur(@Valid @RequestBody UpdateUserRequest request, Authentication authentication) {
        try {
            // Récupérer l'utilisateur depuis la base de données en utilisant son identifiant
            Optional<Utilisateur> utilisateurOptional = utilisateurService.profilUtilisateur(request.getTokenId());
            System.out.println(utilisateurOptional);
            if (utilisateurOptional.isPresent()) {
                Utilisateur utilisateur = utilisateurOptional.get();

                List<String> droits = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

                Utilisateur userUpdated;

                if(droits.contains("GESTION_UTILISATEURS")) {
                    userUpdated = utilisateurService.modifUserByAdmin(utilisateur, request);
                } else {
                    userUpdated = utilisateurService.modifUserByUser(utilisateur, request);
                }

                ProfilUtilisateurDTO profilDTO = utilisateurService.mapUtilisateurToProfilDTO(userUpdated);

                return ResponseEntity.ok(profilDTO);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (EmailDejaUtiliseException e) {
             ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                    .timestamp(Instant.now())
                    .message(e.getMessage())
                    .path("/modifierInfos")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }







}
