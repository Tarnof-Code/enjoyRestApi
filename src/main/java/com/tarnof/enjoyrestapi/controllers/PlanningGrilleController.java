package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.payload.request.*;
import com.tarnof.enjoyrestapi.payload.response.PlanningCelluleDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleDetailDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleSummaryDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningLigneDto;
import com.tarnof.enjoyrestapi.services.PlanningGrilleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sejours/{sejourId}/planning-grilles")
public class PlanningGrilleController {

    private final PlanningGrilleService planningGrilleService;

    public PlanningGrilleController(PlanningGrilleService planningGrilleService) {
        this.planningGrilleService = planningGrilleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public List<PlanningGrilleSummaryDto> lister(
            @PathVariable("sejourId") int sejourId, Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return planningGrilleService.listerGrilles(sejourId, utilisateur.getTokenId());
    }

    @GetMapping("/{grilleId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public PlanningGrilleDetailDto get(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("grilleId") int grilleId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return planningGrilleService.getGrille(sejourId, grilleId, utilisateur.getTokenId());
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanningGrilleDetailDto creer(
            @PathVariable("sejourId") int sejourId, @Valid @RequestBody SavePlanningGrilleRequest request) {
        return planningGrilleService.creerGrille(sejourId, request);
    }

    @PutMapping("/{grilleId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public PlanningGrilleDetailDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("grilleId") int grilleId,
            @Valid @RequestBody UpdatePlanningGrilleRequest request) {
        return planningGrilleService.modifierGrille(sejourId, grilleId, request);
    }

    @DeleteMapping("/{grilleId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable("sejourId") int sejourId, @PathVariable("grilleId") int grilleId) {
        planningGrilleService.supprimerGrille(sejourId, grilleId);
    }

    @PostMapping("/{grilleId}/lignes")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanningLigneDto creerLigne(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("grilleId") int grilleId,
            @Valid @RequestBody SavePlanningLigneRequest request) {
        return planningGrilleService.creerLigne(sejourId, grilleId, request);
    }

    @PutMapping("/{grilleId}/lignes/{ligneId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public PlanningLigneDto modifierLigne(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("grilleId") int grilleId,
            @PathVariable("ligneId") int ligneId,
            @Valid @RequestBody UpdatePlanningLigneRequest request) {
        return planningGrilleService.modifierLigne(sejourId, grilleId, ligneId, request);
    }

    @DeleteMapping("/{grilleId}/lignes/{ligneId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerLigne(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("grilleId") int grilleId,
            @PathVariable("ligneId") int ligneId) {
        planningGrilleService.supprimerLigne(sejourId, grilleId, ligneId);
    }

    @PutMapping("/{grilleId}/lignes/{ligneId}/cellules")
    @PreAuthorize("hasRole('DIRECTION')")
    public List<PlanningCelluleDto> remplacerCellules(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("grilleId") int grilleId,
            @PathVariable("ligneId") int ligneId,
            @Valid @RequestBody UpsertPlanningCellulesRequest request) {
        return planningGrilleService.remplacerCellules(sejourId, grilleId, ligneId, request);
    }
}
