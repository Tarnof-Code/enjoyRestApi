package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantChambreRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantsEnfantsRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantsEquipeRequest;
import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveChambreRequest;
import com.tarnof.enjoyrestapi.payload.response.ChambreDto;
import com.tarnof.enjoyrestapi.services.ChambreService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/chambres")
public class ChambreController {

    private final ChambreService chambreService;

    public ChambreController(ChambreService chambreService) {
        this.chambreService = chambreService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public List<ChambreDto> lister(
            @PathVariable("sejourId") int sejourId, Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return chambreService.listerChambresDuSejour(sejourId, utilisateur.getTokenId());
    }

    @GetMapping("/{chambreId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ChambreDto get(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return chambreService.getChambre(sejourId, chambreId, utilisateur.getTokenId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public ChambreDto creer(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody SaveChambreRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return chambreService.creerChambre(sejourId, request, utilisateur.getTokenId());
    }

    @PutMapping("/{chambreId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ChambreDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @Valid @RequestBody SaveChambreRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return chambreService.modifierChambre(sejourId, chambreId, request, utilisateur.getTokenId());
    }

    @DeleteMapping("/{chambreId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        chambreService.supprimerChambre(sejourId, chambreId, utilisateur.getTokenId());
    }

    @PostMapping("/{chambreId}/referents")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public void ajouterReferent(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @Valid @RequestBody AjouterReferentRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        chambreService.ajouterReferent(sejourId, chambreId, request, utilisateur.getTokenId());
    }

    @DeleteMapping("/{chambreId}/referents/{referentTokenId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerReferent(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @PathVariable("referentTokenId") String referentTokenId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        chambreService.retirerReferent(sejourId, chambreId, referentTokenId, utilisateur.getTokenId());
    }

    @PostMapping("/{chambreId}/occupants/enfants")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ChambreDto affecterEnfants(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @Valid @RequestBody AffecterOccupantsEnfantsRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return chambreService.affecterEnfants(sejourId, chambreId, request, utilisateur.getTokenId());
    }

    @PostMapping("/{chambreId}/occupants/enfants/{enfantId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ChambreDto affecterEnfant(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @PathVariable("enfantId") int enfantId,
            @Valid @RequestBody(required = false) AffecterOccupantChambreRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return chambreService.affecterEnfant(sejourId, chambreId, enfantId, request, utilisateur.getTokenId());
    }

    @DeleteMapping("/{chambreId}/occupants/enfants/{enfantId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerEnfant(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @PathVariable("enfantId") int enfantId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        chambreService.retirerEnfant(sejourId, chambreId, enfantId, utilisateur.getTokenId());
    }

    @PostMapping("/{chambreId}/occupants/equipe")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ChambreDto affecterMembresEquipe(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @Valid @RequestBody AffecterOccupantsEquipeRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return chambreService.affecterMembresEquipe(sejourId, chambreId, request, utilisateur.getTokenId());
    }

    @PostMapping("/{chambreId}/occupants/equipe/{membreTokenId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ChambreDto affecterMembreEquipe(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @PathVariable("membreTokenId") String membreTokenId,
            @Valid @RequestBody(required = false) AffecterOccupantChambreRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return chambreService.affecterMembreEquipe(
                sejourId, chambreId, membreTokenId, request, utilisateur.getTokenId());
    }

    @DeleteMapping("/{chambreId}/occupants/equipe/{membreTokenId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerMembreEquipe(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @PathVariable("membreTokenId") String membreTokenId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        chambreService.retirerMembreEquipe(sejourId, chambreId, membreTokenId, utilisateur.getTokenId());
    }
}
