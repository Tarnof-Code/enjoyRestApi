package com.tarnof.enjoyrestapi.services;

import java.util.List;

import com.tarnof.enjoyrestapi.dto.SejourDTO;
import com.tarnof.enjoyrestapi.enums.RoleSejour;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;
import com.tarnof.enjoyrestapi.payload.request.MembreEquipeRequest;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;

public interface SejourService {
    List<SejourDTO> getAllSejours();
    SejourDTO getSejourById(int id);
    SejourDTO creerSejour(CreateSejourRequest request);
    SejourDTO modifierSejour(int id, CreateSejourRequest request);
    void ajouterMembreEquipe(int sejourId, RegisterRequest registerRequest, MembreEquipeRequest membreEquipeRequest);
    void modifierRoleMembreEquipe(int sejourId, String membreTokenId, RoleSejour nouveauRole);
    void supprimerMembreEquipe(int sejourId, String membreTokenId);
    List<SejourDTO> getSejoursByDirecteur(String directeurTokenId);
    void supprimerSejour(int id);
}
