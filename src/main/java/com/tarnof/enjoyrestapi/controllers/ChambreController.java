package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
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
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.CREATED)
    public ChambreDto creer(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody SaveChambreRequest request) {
        return chambreService.creerChambre(sejourId, request);
    }

    @PutMapping("/{chambreId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    public ChambreDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @Valid @RequestBody SaveChambreRequest request) {
        return chambreService.modifierChambre(sejourId, chambreId, request);
    }

    @DeleteMapping("/{chambreId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId, @PathVariable("chambreId") int chambreId) {
        chambreService.supprimerChambre(sejourId, chambreId);
    }

    @PostMapping("/{chambreId}/referents")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.CREATED)
    public void ajouterReferent(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @Valid @RequestBody AjouterReferentRequest request) {
        chambreService.ajouterReferent(sejourId, chambreId, request);
    }

    @DeleteMapping("/{chambreId}/referents/{referentTokenId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerReferent(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("chambreId") int chambreId,
            @PathVariable("referentTokenId") String referentTokenId) {
        chambreService.retirerReferent(sejourId, chambreId, referentTokenId);
    }
}
