package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.payload.request.SaveLieuRequest;
import com.tarnof.enjoyrestapi.payload.response.LieuDto;
import com.tarnof.enjoyrestapi.services.LieuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/lieux")
public class LieuController {

    private final LieuService lieuService;

    public LieuController(LieuService lieuService) {
        this.lieuService = lieuService;
    }

    @GetMapping
    @PreAuthorize("hasRole('DIRECTION')")
    public List<LieuDto> lister(@PathVariable("sejourId") int sejourId) {
        return lieuService.listerLieuxDuSejour(sejourId);
    }

    @GetMapping("/{lieuId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public LieuDto get(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("lieuId") int lieuId) {
        return lieuService.getLieu(sejourId, lieuId);
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public LieuDto creer(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody SaveLieuRequest request) {
        return lieuService.creerLieu(sejourId, request);
    }

    @PutMapping("/{lieuId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public LieuDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("lieuId") int lieuId,
            @Valid @RequestBody SaveLieuRequest request) {
        return lieuService.modifierLieu(sejourId, lieuId, request);
    }

    @DeleteMapping("/{lieuId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("lieuId") int lieuId) {
        lieuService.supprimerLieu(sejourId, lieuId);
    }
}
