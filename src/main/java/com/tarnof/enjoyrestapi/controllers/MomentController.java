package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.payload.request.ReorderMomentsRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveMomentRequest;
import com.tarnof.enjoyrestapi.payload.response.MomentDto;
import com.tarnof.enjoyrestapi.services.MomentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/moments")
public class MomentController {

    private final MomentService momentService;

    public MomentController(MomentService momentService) {
        this.momentService = momentService;
    }

    @GetMapping
    @PreAuthorize("hasRole('DIRECTION')")
    public List<MomentDto> lister(@PathVariable("sejourId") int sejourId) {
        return momentService.listerMomentsDuSejour(sejourId);
    }

    @GetMapping("/{momentId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public MomentDto get(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("momentId") int momentId) {
        return momentService.getMoment(sejourId, momentId);
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public MomentDto creer(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody SaveMomentRequest request) {
        return momentService.creerMoment(sejourId, request);
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('DIRECTION')")
    public List<MomentDto> reorder(
            @PathVariable("sejourId") int sejourId,
            @Valid @RequestBody ReorderMomentsRequest request) {
        return momentService.reorderMoments(sejourId, request);
    }

    @PutMapping("/{momentId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public MomentDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("momentId") int momentId,
            @Valid @RequestBody SaveMomentRequest request) {
        return momentService.modifierMoment(sejourId, momentId, request);
    }

    @DeleteMapping("/{momentId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("momentId") int momentId) {
        momentService.supprimerMoment(sejourId, momentId);
    }
}
