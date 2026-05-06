package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.payload.request.CreateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.ActiviteDto;
import com.tarnof.enjoyrestapi.services.ActiviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/activites")
public class ActiviteController {

    private final ActiviteService activiteService;

    public ActiviteController(ActiviteService activiteService) {
        this.activiteService = activiteService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public List<ActiviteDto> lister(@PathVariable("sejourId") int sejourId, Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return activiteService.listerActivitesDuSejour(sejourId, utilisateur.getTokenId());
    }

    @GetMapping("/{activiteId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ActiviteDto get(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("activiteId") int activiteId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return activiteService.getActivite(sejourId, activiteId, utilisateur.getTokenId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ActiviteDto creer(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody CreateActiviteRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return activiteService.creerActivite(sejourId, request, utilisateur.getTokenId());
    }

    @PutMapping("/{activiteId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ActiviteDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("activiteId") int activiteId,
            @Valid @RequestBody UpdateActiviteRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return activiteService.modifierActivite(sejourId, activiteId, request, utilisateur.getTokenId());
    }

    @DeleteMapping("/{activiteId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("activiteId") int activiteId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        activiteService.supprimerActivite(sejourId, activiteId, utilisateur.getTokenId());
    }
}
