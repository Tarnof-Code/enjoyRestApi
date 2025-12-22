package com.tarnof.enjoyrestapi.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tarnof.enjoyrestapi.dto.SejourDTO;
import com.tarnof.enjoyrestapi.payload.request.MembreEquipeRequest;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import com.tarnof.enjoyrestapi.services.SejourService;
    
import jakarta.validation.Valid;

@RestController
@RequestMapping("api/v1/sejours")
public class SejourController {
    @Autowired
    private SejourService sejourService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SejourDTO> getAllSejours() {
        return sejourService.getAllSejours();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTION')")
    public SejourDTO getSejourById(@PathVariable int id) {
        return sejourService.getSejourById(id);
    }

    @PostMapping("/{id}/equipe/existant")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public void ajouterMembreExistant(@PathVariable("id") int sejourId, @Valid @RequestBody MembreEquipeRequest request) {
        sejourService.ajouterMembreEquipe(sejourId, null, request);
    }

    @PostMapping("/{id}/equipe/nouveau")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public void ajouterNouveauMembre(@PathVariable("id") int sejourId, @Valid @RequestBody RegisterRequest request) {
        sejourService.ajouterMembreEquipe(sejourId, request, null);
    }

    @PutMapping("/{id}/equipe/{membreTokenId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void modifierRoleMembreEquipe(
            @PathVariable("id") int sejourId, 
            @PathVariable("membreTokenId") String membreTokenId,
            @Valid @RequestBody MembreEquipeRequest request) {
        sejourService.modifierRoleMembreEquipe(sejourId, membreTokenId, request.getRoleSejour());
    }

    @DeleteMapping("/{id}/equipe/{membreTokenId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerMembreEquipe(@PathVariable("id") int sejourId, @PathVariable("membreTokenId") String membreTokenId) {
        sejourService.supprimerMembreEquipe(sejourId, membreTokenId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SejourDTO creerSejour(@RequestBody CreateSejourRequest request) {
        return sejourService.creerSejour(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SejourDTO modifierSejour(@PathVariable int id, @RequestBody CreateSejourRequest request) {
        return sejourService.modifierSejour(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerSejour(@PathVariable int id) {
        sejourService.supprimerSejour(id);
    }

    @GetMapping("/directeur/{directeurTokenId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public List<SejourDTO> getSejoursByDirecteur(@PathVariable String directeurTokenId) {
        return sejourService.getSejoursByDirecteur(directeurTokenId);
    }
}
