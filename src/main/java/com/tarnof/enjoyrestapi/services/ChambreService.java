package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveChambreRequest;
import com.tarnof.enjoyrestapi.payload.response.ChambreDto;

import java.util.List;

public interface ChambreService {

    List<ChambreDto> listerChambresDuSejour(int sejourId, String utilisateurTokenId);

    ChambreDto getChambre(int sejourId, int chambreId, String utilisateurTokenId);

    ChambreDto creerChambre(int sejourId, SaveChambreRequest request);

    ChambreDto modifierChambre(int sejourId, int chambreId, SaveChambreRequest request);

    void supprimerChambre(int sejourId, int chambreId);

    void ajouterReferent(int sejourId, int chambreId, AjouterReferentRequest request);

    void retirerReferent(int sejourId, int chambreId, String referentTokenId);
}
