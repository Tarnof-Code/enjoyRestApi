package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.payload.request.SaveActivitePrestataireRequest;
import com.tarnof.enjoyrestapi.payload.response.ActivitePrestataireDto;
import com.tarnof.enjoyrestapi.services.ActivitePrestataireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/activites-prestataires")
public class ActivitePrestataireController {

    private final ActivitePrestataireService activitePrestataireService;

    public ActivitePrestataireController(ActivitePrestataireService activitePrestataireService) {
        this.activitePrestataireService = activitePrestataireService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public List<ActivitePrestataireDto> lister(
            @PathVariable("sejourId") int sejourId, Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return activitePrestataireService.listerActivitesPrestatairesDuSejour(sejourId, utilisateur.getTokenId());
    }

    @GetMapping("/{activitePrestataireId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ActivitePrestataireDto get(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("activitePrestataireId") int activitePrestataireId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return activitePrestataireService.getActivitePrestataire(
                sejourId, activitePrestataireId, utilisateur.getTokenId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.CREATED)
    public ActivitePrestataireDto creer(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody SaveActivitePrestataireRequest request) {
        return activitePrestataireService.creerActivitePrestataire(sejourId, request);
    }

    @PutMapping("/{activitePrestataireId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    public ActivitePrestataireDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("activitePrestataireId") int activitePrestataireId,
            @Valid @RequestBody SaveActivitePrestataireRequest request) {
        return activitePrestataireService.modifierActivitePrestataire(sejourId, activitePrestataireId, request);
    }

    @DeleteMapping("/{activitePrestataireId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("activitePrestataireId") int activitePrestataireId) {
        activitePrestataireService.supprimerActivitePrestataire(sejourId, activitePrestataireId);
    }
}
