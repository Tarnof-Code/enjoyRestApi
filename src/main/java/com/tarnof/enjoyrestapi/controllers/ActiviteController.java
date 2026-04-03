package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.payload.request.CreateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.ActiviteDto;
import com.tarnof.enjoyrestapi.services.ActiviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/activites")
public class ActiviteController {

    private final ActiviteService activiteService;

    public ActiviteController(ActiviteService activiteService) {
        this.activiteService = activiteService;
    }

    @GetMapping
    @PreAuthorize("hasRole('DIRECTION')")
    public List<ActiviteDto> lister(@PathVariable("sejourId") int sejourId) {
        return activiteService.listerActivitesDuSejour(sejourId);
    }

    @GetMapping("/{activiteId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public ActiviteDto get(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("activiteId") int activiteId) {
        return activiteService.getActivite(sejourId, activiteId);
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public ActiviteDto creer(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody CreateActiviteRequest request) {
        return activiteService.creerActivite(sejourId, request);
    }

    @PutMapping("/{activiteId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public ActiviteDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("activiteId") int activiteId,
            @Valid @RequestBody UpdateActiviteRequest request) {
        return activiteService.modifierActivite(sejourId, activiteId, request);
    }

    @DeleteMapping("/{activiteId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("activiteId") int activiteId) {
        activiteService.supprimerActivite(sejourId, activiteId);
    }
}
