package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.payload.request.SaveTypeActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.TypeActiviteDto;
import com.tarnof.enjoyrestapi.services.TypeActiviteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/types-activite")
public class TypeActiviteController {

    private final TypeActiviteService typeActiviteService;

    public TypeActiviteController(TypeActiviteService typeActiviteService) {
        this.typeActiviteService = typeActiviteService;
    }

    @GetMapping
    @PreAuthorize("hasRole('DIRECTION')")
    public List<TypeActiviteDto> lister(@PathVariable("sejourId") int sejourId) {
        return typeActiviteService.listerTypesActivite(sejourId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTION')")
    public TypeActiviteDto get(@PathVariable("sejourId") int sejourId, @PathVariable("id") int id) {
        return typeActiviteService.getTypeActivite(sejourId, id);
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public TypeActiviteDto creer(
            @PathVariable("sejourId") int sejourId, @Valid @RequestBody SaveTypeActiviteRequest request) {
        return typeActiviteService.creerTypeActivite(sejourId, request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTION')")
    public TypeActiviteDto modifier(
            @PathVariable("sejourId") int sejourId,
            @PathVariable("id") int id,
            @Valid @RequestBody SaveTypeActiviteRequest request) {
        return typeActiviteService.modifierTypeActivite(sejourId, id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable("sejourId") int sejourId, @PathVariable("id") int id) {
        typeActiviteService.supprimerTypeActivite(sejourId, id);
    }
}
