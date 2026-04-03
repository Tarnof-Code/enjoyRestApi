package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.CreateGroupeRequest;
import com.tarnof.enjoyrestapi.payload.response.GroupeDto;
import com.tarnof.enjoyrestapi.services.GroupeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/groupes")
public class GroupeController {

    private final GroupeService groupeService;

    public GroupeController(GroupeService groupeService) {
        this.groupeService = groupeService;
    }

    @GetMapping
    @PreAuthorize("hasRole('DIRECTION')")
    public List<GroupeDto> getGroupesDuSejour(@PathVariable("sejourId") int sejourId) {
        return groupeService.getGroupesDuSejour(sejourId);
    }

    @GetMapping("/{groupeId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public GroupeDto getGroupeById(@PathVariable("sejourId") int sejourId, @PathVariable("groupeId") int groupeId) {
        return groupeService.getGroupeById(sejourId, groupeId);
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public GroupeDto creerGroupe(@PathVariable("sejourId") int sejourId, @Valid @RequestBody CreateGroupeRequest request) {
        return groupeService.creerGroupe(sejourId, request);
    }

    @PutMapping("/{groupeId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public GroupeDto modifierGroupe(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("groupeId") int groupeId,
            @Valid @RequestBody CreateGroupeRequest request) {
        return groupeService.modifierGroupe(sejourId, groupeId, request);
    }

    @DeleteMapping("/{groupeId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerGroupe(@PathVariable("sejourId") int sejourId, @PathVariable("groupeId") int groupeId) {
        groupeService.supprimerGroupe(sejourId, groupeId);
    }

    @PostMapping("/{groupeId}/enfants/{enfantId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ajouterEnfantAuGroupe(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("groupeId") int groupeId,
            @PathVariable("enfantId") int enfantId) {
        groupeService.ajouterEnfantAuGroupe(sejourId, groupeId, enfantId);
    }

    @DeleteMapping("/{groupeId}/enfants/{enfantId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerEnfantDuGroupe(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("groupeId") int groupeId,
            @PathVariable("enfantId") int enfantId) {
        groupeService.retirerEnfantDuGroupe(sejourId, groupeId, enfantId);
    }

    @PostMapping("/{groupeId}/referents")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public void ajouterReferent(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("groupeId") int groupeId,
            @Valid @RequestBody AjouterReferentRequest request) {
        groupeService.ajouterReferent(sejourId, groupeId, request);
    }

    @DeleteMapping("/{groupeId}/referents/{referentTokenId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirerReferent(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("groupeId") int groupeId,
            @PathVariable("referentTokenId") String referentTokenId) {
        groupeService.retirerReferent(sejourId, groupeId, referentTokenId);
    }
}
