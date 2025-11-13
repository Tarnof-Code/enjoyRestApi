package com.tarnof.enjoyrestapi.controllers;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;
import com.tarnof.enjoyrestapi.services.SejourService;

@RestController
@RequestMapping("api/v1/sejours")
public class SejourController {
    @Autowired
    private SejourService sejourService;

    @GetMapping("/liste")
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    public List<Sejour> getAllSejours() {
        return sejourService.getAllSejours();
    }

    @PostMapping("/creer")
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    public Sejour creerSejour(@RequestBody CreateSejourRequest request) {
        return sejourService.creerSejour(request);
    }

    @PutMapping("/modifier/{id}")
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    public Sejour modifierSejour(@PathVariable int id, @RequestBody CreateSejourRequest request) {
        return sejourService.modifierSejour(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('GESTION_UTILISATEURS')")
    public ResponseEntity<?> supprimerSejour(@PathVariable int id) {
        try {
            sejourService.supprimerSejour(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
