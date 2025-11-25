package com.tarnof.enjoyrestapi.services;

import java.util.List;

import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;

public interface SejourService {
    List<Sejour> getAllSejours();
    Sejour creerSejour(CreateSejourRequest request);
    Sejour modifierSejour(int id, CreateSejourRequest request);
    List<Sejour> getSejoursByDirecteur(String directeurTokenId);
    void supprimerSejour(int id);
}
