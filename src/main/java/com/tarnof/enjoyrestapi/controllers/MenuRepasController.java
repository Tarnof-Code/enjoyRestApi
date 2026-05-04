package com.tarnof.enjoyrestapi.controllers;

import com.tarnof.enjoyrestapi.payload.request.SaveMenuRepasRequest;
import com.tarnof.enjoyrestapi.payload.response.MenuRepasDto;
import com.tarnof.enjoyrestapi.services.MenuRepasService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("api/v1/sejours/{sejourId}/menus")
public class MenuRepasController {

    private final MenuRepasService menuRepasService;

    public MenuRepasController(MenuRepasService menuRepasService) {
        this.menuRepasService = menuRepasService;
    }

    @GetMapping
    @PreAuthorize("hasRole('DIRECTION')")
    public List<MenuRepasDto> lister(
            @PathVariable int sejourId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        if (date != null) {
            return menuRepasService.listerParJour(sejourId, date);
        }
        if (dateDebut != null && dateFin != null) {
            return menuRepasService.listerParPeriode(sejourId, dateDebut, dateFin);
        }
        throw new IllegalArgumentException(
                "Indiquez le paramètre date (un jour) ou bien dateDebut et dateFin (une période).");
    }

    @GetMapping("/{menuId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public MenuRepasDto get(@PathVariable int sejourId, @PathVariable int menuId) {
        return menuRepasService.get(sejourId, menuId);
    }

    @PostMapping
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.CREATED)
    public MenuRepasDto creer(@PathVariable int sejourId, @Valid @RequestBody SaveMenuRepasRequest request) {
        return menuRepasService.creer(sejourId, request);
    }

    @PutMapping("/{menuId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public MenuRepasDto modifier(
            @PathVariable int sejourId,
            @PathVariable int menuId,
            @Valid @RequestBody SaveMenuRepasRequest request) {
        return menuRepasService.modifier(sejourId, menuId, request);
    }

    @DeleteMapping("/{menuId}")
    @PreAuthorize("hasRole('DIRECTION')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable int sejourId, @PathVariable int menuId) {
        menuRepasService.supprimer(sejourId, menuId);
    }
}
