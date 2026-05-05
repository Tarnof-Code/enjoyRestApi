package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.payload.request.SaveHoraireRequest;
import com.tarnof.enjoyrestapi.payload.response.HoraireDto;
import com.tarnof.enjoyrestapi.services.HoraireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/horaires")
public class HoraireController {

    private final HoraireService horaireService;

    public HoraireController(HoraireService horaireService) {
        this.horaireService = horaireService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public List<HoraireDto> lister(@PathVariable("sejourId") int sejourId, Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return horaireService.listerHorairesDuSejour(sejourId, utilisateur.getTokenId());
    }

    @GetMapping("/{horaireId}")
    @PreAuthorize("hasAuthority('ACCES_SEJOUR')")
    public HoraireDto get(
            @PathVariable("sejourId") int sejourId, @PathVariable("horaireId") int horaireId, Authentication authentication) {
        Utilisateur utilisateur = (Utilisateur) authentication.getPrincipal();
        return horaireService.getHoraire(sejourId, horaireId, utilisateur.getTokenId());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.CREATED)
    public HoraireDto creer(
            @PathVariable("sejourId") int sejourId, @Valid @RequestBody SaveHoraireRequest request) {
        return horaireService.creerHoraire(sejourId, request);
    }

    @PutMapping("/{horaireId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    public HoraireDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("horaireId") int horaireId,
            @Valid @RequestBody SaveHoraireRequest request) {
        return horaireService.modifierHoraire(sejourId, horaireId, request);
    }

    @DeleteMapping("/{horaireId}")
    @PreAuthorize("hasAuthority('GESTION_SEJOURS')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId, @PathVariable("horaireId") int horaireId) {
        horaireService.supprimerHoraire(sejourId, horaireId);
    }
}
