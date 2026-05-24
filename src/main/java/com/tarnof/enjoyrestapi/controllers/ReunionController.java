package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.payload.request.SaveReunionRequest;
import com.tarnof.enjoyrestapi.payload.response.ReunionDto;
import com.tarnof.enjoyrestapi.services.ReunionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/reunions")
public class ReunionController {

    private final ReunionService reunionService;

    public ReunionController(ReunionService reunionService) {
        this.reunionService = reunionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public List<ReunionDto> lister(@PathVariable("sejourId") int sejourId, Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return reunionService.listerReunionsDuSejour(sejourId, utilisateur.getTokenId());
    }

    @GetMapping("/{reunionId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public ReunionDto get(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("reunionId") int reunionId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return reunionService.getReunion(sejourId, reunionId, utilisateur.getTokenId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.CREATED)
    public ReunionDto creer(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody SaveReunionRequest request) {
        return reunionService.creerReunion(sejourId, request);
    }

    @PutMapping("/{reunionId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    public ReunionDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("reunionId") int reunionId,
            @Valid @RequestBody SaveReunionRequest request) {
        return reunionService.modifierReunion(sejourId, reunionId, request);
    }

    @DeleteMapping("/{reunionId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("reunionId") int reunionId) {
        reunionService.supprimerReunion(sejourId, reunionId);
    }
}
