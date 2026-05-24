package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.SaveReunionRequest;
import com.tarnof.enjoyrestapi.payload.response.ReunionDto;

import java.util.List;

public interface ReunionService {

    List<ReunionDto> listerReunionsDuSejour(int sejourId, String utilisateurTokenId);

    ReunionDto getReunion(int sejourId, int reunionId, String utilisateurTokenId);

    ReunionDto creerReunion(int sejourId, SaveReunionRequest request);

    ReunionDto modifierReunion(int sejourId, int reunionId, SaveReunionRequest request);

    void supprimerReunion(int sejourId, int reunionId);
}
