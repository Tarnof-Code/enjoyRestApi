package com.tarnof.enjoyrestapi.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.tarnof.enjoyrestapi.dto.SejourDTO;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;
import com.tarnof.enjoyrestapi.services.SejourService;

@RestController
@RequestMapping("api/v1/sejours")
public class SejourController {
    @Autowired
    private SejourService sejourService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<SejourDTO> getAllSejours() {
        return sejourService.getAllSejours();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DIRECTION')")
    public SejourDTO getSejourById(@PathVariable int id) {
        return sejourService.getSejourById(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public SejourDTO creerSejour(@RequestBody CreateSejourRequest request) {
        return sejourService.creerSejour(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public SejourDTO modifierSejour(@PathVariable int id, @RequestBody CreateSejourRequest request) {
        return sejourService.modifierSejour(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> supprimerSejour(@PathVariable int id) {
        try {
            sejourService.supprimerSejour(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/directeur/{directeurTokenId}")
    @PreAuthorize("hasRole('DIRECTION')")
    public List<SejourDTO> getSejoursByDirecteur(@PathVariable String directeurTokenId) {
        return sejourService.getSejoursByDirecteur(directeurTokenId);
    }
}
