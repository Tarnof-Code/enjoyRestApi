package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.enums.TypeReferenceAlimentaire;
import com.tarnof.enjoyrestapi.payload.request.SaveReferenceAlimentaireRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateReferenceAlimentaireRequest;
import com.tarnof.enjoyrestapi.payload.response.ReferenceAlimentaireDto;
import com.tarnof.enjoyrestapi.services.ReferenceAlimentaireService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/references-alimentaires")
public class ReferenceAlimentaireController {

    private final ReferenceAlimentaireService referenceAlimentaireService;

    public ReferenceAlimentaireController(ReferenceAlimentaireService referenceAlimentaireService) {
        this.referenceAlimentaireService = referenceAlimentaireService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTION')")
    public List<ReferenceAlimentaireDto> lister(@RequestParam(required = false) TypeReferenceAlimentaire type) {
        return referenceAlimentaireService.lister(type);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTION')")
    public ReferenceAlimentaireDto get(@PathVariable int id) {
        return referenceAlimentaireService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public ReferenceAlimentaireDto creer(@Valid @RequestBody SaveReferenceAlimentaireRequest request) {
        return referenceAlimentaireService.creer(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTION')")
    public ReferenceAlimentaireDto modifier(
            @PathVariable int id, @Valid @RequestBody UpdateReferenceAlimentaireRequest request) {
        return referenceAlimentaireService.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable int id) {
        referenceAlimentaireService.supprimer(id);
    }
}
