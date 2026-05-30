package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantChambreRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantsEnfantsRequest;
import com.tarnof.enjoyrestapi.payload.request.AffecterOccupantsEquipeRequest;
import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.SaveChambreRequest;
import com.tarnof.enjoyrestapi.payload.response.ChambreDto;

import java.util.List;

public interface ChambreService {

    List<ChambreDto> listerChambresDuSejour(int sejourId, String utilisateurTokenId);

    ChambreDto getChambre(int sejourId, int chambreId, String utilisateurTokenId);

    ChambreDto creerChambre(int sejourId, SaveChambreRequest request, String utilisateurTokenId);

    ChambreDto modifierChambre(int sejourId, int chambreId, SaveChambreRequest request, String utilisateurTokenId);

    void supprimerChambre(int sejourId, int chambreId, String utilisateurTokenId);

    void ajouterReferent(int sejourId, int chambreId, AjouterReferentRequest request, String utilisateurTokenId);

    void retirerReferent(int sejourId, int chambreId, String referentTokenId, String utilisateurTokenId);

    ChambreDto affecterEnfant(
            int sejourId,
            int chambreId,
            int enfantId,
            AffecterOccupantChambreRequest request,
            String utilisateurTokenId);

    ChambreDto affecterEnfants(
            int sejourId, int chambreId, AffecterOccupantsEnfantsRequest request, String utilisateurTokenId);

    void retirerEnfant(int sejourId, int chambreId, int enfantId, String utilisateurTokenId);

    ChambreDto affecterMembreEquipe(
            int sejourId,
            int chambreId,
            String membreTokenId,
            AffecterOccupantChambreRequest request,
            String utilisateurTokenId);

    ChambreDto affecterMembresEquipe(
            int sejourId, int chambreId, AffecterOccupantsEquipeRequest request, String utilisateurTokenId);

    void retirerMembreEquipe(int sejourId, int chambreId, String membreTokenId, String utilisateurTokenId);
}
