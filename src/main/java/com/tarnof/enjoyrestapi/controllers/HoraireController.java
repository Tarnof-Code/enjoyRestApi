package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.payload.request.SaveHoraireRequest;
import com.tarnof.enjoyrestapi.payload.response.HoraireDto;
import com.tarnof.enjoyrestapi.services.HoraireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('DIRECTION')")
    public List<HoraireDto> lister(@PathVariable("sejourId") int sejourId) {
        return horaireService.listerHorairesDuSejour(sejourId);
    }

    @GetMapping("/{horaireId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public HoraireDto get(
            @PathVariable("sejourId") int sejourId, @PathVariable("horaireId") int horaireId) {
        return horaireService.getHoraire(sejourId, horaireId);
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public HoraireDto creer(
            @PathVariable("sejourId") int sejourId, @Valid @RequestBody SaveHoraireRequest request) {
        return horaireService.creerHoraire(sejourId, request);
    }

    @PutMapping("/{horaireId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public HoraireDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("horaireId") int horaireId,
            @Valid @RequestBody SaveHoraireRequest request) {
        return horaireService.modifierHoraire(sejourId, horaireId, request);
    }

    @DeleteMapping("/{horaireId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId, @PathVariable("horaireId") int horaireId) {
        horaireService.supprimerHoraire(sejourId, horaireId);
    }
}
