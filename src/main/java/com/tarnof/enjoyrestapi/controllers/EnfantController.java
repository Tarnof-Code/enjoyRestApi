package com.tarnof.enjoyrestapi.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import com.tarnof.enjoyrestapi.entities.Utilisateur;

import com.tarnof.enjoyrestapi.excel.ExcelImportSpec;
import com.tarnof.enjoyrestapi.payload.response.DossierEnfantDto;
import com.tarnof.enjoyrestapi.payload.response.EnfantDto;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportResponse;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportSpecResponse;
import com.tarnof.enjoyrestapi.payload.request.CreateEnfantRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateDossierEnfantRequest;
import com.tarnof.enjoyrestapi.services.EnfantService;
    
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/enfants")
public class EnfantController {
    
    private final EnfantService enfantService;

    public EnfantController(EnfantService enfantService) {
        this.enfantService = enfantService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public List<EnfantDto> getEnfantsDuSejour(@PathVariable("sejourId") int sejourId, Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return enfantService.getEnfantsDuSejour(sejourId, utilisateur.getTokenId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.CREATED)
    public void creerEtAjouterEnfantAuSejour(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody CreateEnfantRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        enfantService.creerEtAjouterEnfantAuSejour(sejourId, request, utilisateur.getTokenId());
    }

    @GetMapping("/{enfantId}/dossier")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public DossierEnfantDto getDossierEnfant(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("enfantId") int enfantId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return enfantService.getDossierEnfant(sejourId, enfantId, utilisateur.getTokenId());
    }

    @PutMapping("/{enfantId}/dossier")
    @PreAuthorize("hasAuthority('GESTION_SANITAIRE')")
    public DossierEnfantDto modifierDossierEnfant(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("enfantId") int enfantId,
            @Valid @RequestBody UpdateDossierEnfantRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return enfantService.modifierDossierEnfant(sejourId, enfantId, request, utilisateur.getTokenId());
    }

    @PutMapping("/{enfantId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    public EnfantDto modifierEnfant(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("enfantId") int enfantId,
            @Valid @RequestBody CreateEnfantRequest request,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return enfantService.modifierEnfant(sejourId, enfantId, request, utilisateur.getTokenId());
    }

    @DeleteMapping("/{enfantId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerEnfantDuSejour(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("enfantId") int enfantId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        enfantService.supprimerEnfantDuSejour(sejourId, enfantId, utilisateur.getTokenId());
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerTousLesEnfantsDuSejour(
            @PathVariable("sejourId") int sejourId,
            Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        enfantService.supprimerTousLesEnfantsDuSejour(sejourId, utilisateur.getTokenId());
    }

    @GetMapping("/import/spec")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    public ExcelImportSpecResponse getExcelImportSpec() {
        return ExcelImportSpec.getInstance().getSpecForApi();
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    public ExcelImportResponse importerEnfantsDepuisExcel(
            @PathVariable("sejourId") int sejourId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier Excel est vide");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") &&
             !contentType.equals("application/vnd.ms-excel"))) {
            throw new IllegalArgumentException("Le fichier doit être un fichier Excel (.xlsx ou .xls)");
        }
        
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return enfantService.importerEnfantsDepuisExcel(sejourId, file, utilisateur.getTokenId());
    }
}
