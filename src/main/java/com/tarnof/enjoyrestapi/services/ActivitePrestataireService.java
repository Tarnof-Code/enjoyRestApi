package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.SaveActivitePrestataireRequest;
import com.tarnof.enjoyrestapi.payload.response.ActivitePrestataireDto;

import java.util.List;

public interface ActivitePrestataireService {

    List<ActivitePrestataireDto> listerActivitesPrestatairesDuSejour(int sejourId, String utilisateurTokenId);

    ActivitePrestataireDto getActivitePrestataire(int sejourId, int activitePrestataireId, String utilisateurTokenId);

    ActivitePrestataireDto creerActivitePrestataire(
            int sejourId, SaveActivitePrestataireRequest request, String utilisateurTokenId);

    ActivitePrestataireDto modifierActivitePrestataire(
            int sejourId, int activitePrestataireId, SaveActivitePrestataireRequest request, String utilisateurTokenId);

    void supprimerActivitePrestataire(int sejourId, int activitePrestataireId, String utilisateurTokenId);
}
