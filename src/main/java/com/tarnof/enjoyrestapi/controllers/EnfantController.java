package com.tarnof.enjoyrestapi.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import com.tarnof.enjoyrestapi.payload.response.EnfantDto;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportResponse;
import com.tarnof.enjoyrestapi.payload.request.CreateEnfantRequest;
import com.tarnof.enjoyrestapi.services.EnfantService;
    
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/enfants")
@RequiredArgsConstructor
public class EnfantController {
    
    private final EnfantService enfantService;

    @GetMapping
    @PreAuthorize("hasRole('DIRECTION')")
    public List<EnfantDto> getEnfantsDuSejour(@PathVariable("sejourId") int sejourId) {
        return enfantService.getEnfantsDuSejour(sejourId);
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public void creerEtAjouterEnfantAuSejour(@PathVariable("sejourId") int sejourId, @Valid @RequestBody CreateEnfantRequest request) {
        enfantService.creerEtAjouterEnfantAuSejour(sejourId, request);
    }

    @PutMapping("/{enfantId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public EnfantDto modifierEnfant(@PathVariable("sejourId") int sejourId, @PathVariable("enfantId") int enfantId, @Valid @RequestBody CreateEnfantRequest request) {
        return enfantService.modifierEnfant(sejourId, enfantId, request);
    }

    @DeleteMapping("/{enfantId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerEnfantDuSejour(@PathVariable("sejourId") int sejourId, @PathVariable("enfantId") int enfantId) {
        enfantService.supprimerEnfantDuSejour(sejourId, enfantId);
    }

    @DeleteMapping("/all")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerTousLesEnfantsDuSejour(@PathVariable("sejourId") int sejourId) {
        enfantService.supprimerTousLesEnfantsDuSejour(sejourId);
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('DIRECTION')")
    public ExcelImportResponse importerEnfantsDepuisExcel(
            @PathVariable("sejourId") int sejourId,
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Le fichier Excel est vide");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") &&
             !contentType.equals("application/vnd.ms-excel"))) {
            throw new IllegalArgumentException("Le fichier doit être un fichier Excel (.xlsx ou .xls)");
        }
        
        return enfantService.importerEnfantsDepuisExcel(sejourId, file);
    }
}
