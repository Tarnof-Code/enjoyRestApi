package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.payload.response.ReferencesAlimentairesAgregeesEnfantsDto;
import com.tarnof.enjoyrestapi.services.ReferencesAlimentairesAgregeesEnfantsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/references-alimentaires-agregees-enfants")
public class ReferencesAlimentairesAgregeesEnfantsController {

    private final ReferencesAlimentairesAgregeesEnfantsService referencesAlimentairesAgregeesEnfantsService;

    public ReferencesAlimentairesAgregeesEnfantsController(
            ReferencesAlimentairesAgregeesEnfantsService referencesAlimentairesAgregeesEnfantsService) {
        this.referencesAlimentairesAgregeesEnfantsService = referencesAlimentairesAgregeesEnfantsService;
    }

    /**
     * Allergènes et régimes/préférences présents sur au moins un dossier d’un enfant inscrit au séjour (sans doublon).
     * Utile pour restreindre les tags proposés lors de la saisie des menus.
     */
    @GetMapping
    @PreAuthorize("hasRole('DIRECTION')")
    public ReferencesAlimentairesAgregeesEnfantsDto get(@PathVariable int sejourId) {
        return referencesAlimentairesAgregeesEnfantsService.agregerPourSejour(sejourId);
    }
}
