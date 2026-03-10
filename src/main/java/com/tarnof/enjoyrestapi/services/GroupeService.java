package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.CreateGroupeRequest;
import com.tarnof.enjoyrestapi.payload.response.GroupeDto;

import java.util.List;

public interface GroupeService {
    List<GroupeDto> getGroupesDuSejour(int sejourId);
    GroupeDto getGroupeById(int sejourId, int groupeId);
    GroupeDto creerGroupe(int sejourId, CreateGroupeRequest request);
    GroupeDto modifierGroupe(int sejourId, int groupeId, CreateGroupeRequest request);
    void supprimerGroupe(int sejourId, int groupeId);
    void ajouterEnfantAuGroupe(int sejourId, int groupeId, int enfantId);
    void retirerEnfantDuGroupe(int sejourId, int groupeId, int enfantId);
    void ajouterReferent(int sejourId, int groupeId, AjouterReferentRequest request);
    void retirerReferent(int sejourId, int groupeId, String referentTokenId);
}
