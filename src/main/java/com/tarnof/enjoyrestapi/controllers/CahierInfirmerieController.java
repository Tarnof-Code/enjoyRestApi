package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.payload.request.SaveCahierInfirmerieEntreeRequest;
import com.tarnof.enjoyrestapi.payload.response.CahierInfirmerieEntreeDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationCahierInfirmerieDto;
import com.tarnof.enjoyrestapi.services.CahierInfirmerieService;
import com.tarnof.enjoyrestapi.services.HistoriqueModificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/cahier-infirmerie")
public class CahierInfirmerieController {

    private final CahierInfirmerieService cahierInfirmerieService;
    private final HistoriqueModificationService historiqueModificationService;

    public CahierInfirmerieController(
            CahierInfirmerieService cahierInfirmerieService,
            HistoriqueModificationService historiqueModificationService) {
        this.cahierInfirmerieService = cahierInfirmerieService;
        this.historiqueModificationService = historiqueModificationService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public List<CahierInfirmerieEntreeDto> lister(
            @PathVariable("sejourId") int sejourId, Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return cahierInfirmerieService.listerEntreesDuSejour(sejourId, utilisateur.getTokenId());
    }

    @GetMapping("/{entreeId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public CahierInfirmerieEntreeDto get(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("entreeId") int entreeId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return cahierInfirmerieService.getEntree(sejourId, entreeId, utilisateur.getTokenId());
    }

    @GetMapping("/{entreeId}/historique")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public List<HistoriqueModificationCahierInfirmerieDto> historique(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("entreeId") int entreeId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return historiqueModificationService.listerHistoriqueCahierInfirmerie(
                sejourId, entreeId, utilisateur.getTokenId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.CREATED)
    public CahierInfirmerieEntreeDto creer(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody SaveCahierInfirmerieEntreeRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return cahierInfirmerieService.creerEntree(sejourId, request, utilisateur.getTokenId());
    }

    @PutMapping("/{entreeId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public CahierInfirmerieEntreeDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("entreeId") int entreeId,
            @Valid @RequestBody SaveCahierInfirmerieEntreeRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return cahierInfirmerieService.modifierEntree(sejourId, entreeId, request, utilisateur.getTokenId());
    }

    @DeleteMapping("/{entreeId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("entreeId") int entreeId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        cahierInfirmerieService.supprimerEntree(sejourId, entreeId, utilisateur.getTokenId());
    }
}
