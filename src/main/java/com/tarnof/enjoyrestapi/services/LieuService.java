package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.SaveLieuRequest;
import com.tarnof.enjoyrestapi.payload.response.LieuDto;

import java.util.List;

public interface LieuService {
    List<LieuDto> listerLieuxDuSejour(int sejourId, String utilisateurTokenId);

    LieuDto getLieu(int sejourId, int lieuId, String utilisateurTokenId);

    LieuDto creerLieu(int sejourId, SaveLieuRequest request);

    LieuDto modifierLieu(int sejourId, int lieuId, SaveLieuRequest request);

    void supprimerLieu(int sejourId, int lieuId);
}
