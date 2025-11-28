package com.tarnof.enjoyrestapi.services;

import java.util.List;

import com.tarnof.enjoyrestapi.dto.SejourDTO;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;

public interface SejourService {
    List<SejourDTO> getAllSejours();
    SejourDTO getSejourById(int id);
    SejourDTO creerSejour(CreateSejourRequest request);
    SejourDTO modifierSejour(int id, CreateSejourRequest request);
    List<SejourDTO> getSejoursByDirecteur(String directeurTokenId);
    void supprimerSejour(int id);
}
