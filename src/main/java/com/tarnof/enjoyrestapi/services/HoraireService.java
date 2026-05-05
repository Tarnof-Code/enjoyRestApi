package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.SaveHoraireRequest;
import com.tarnof.enjoyrestapi.payload.response.HoraireDto;

import java.util.List;

public interface HoraireService {
    List<HoraireDto> listerHorairesDuSejour(int sejourId, String utilisateurTokenId);

    HoraireDto getHoraire(int sejourId, int horaireId, String utilisateurTokenId);

    HoraireDto creerHoraire(int sejourId, SaveHoraireRequest request);

    HoraireDto modifierHoraire(int sejourId, int horaireId, SaveHoraireRequest request);

    void supprimerHoraire(int sejourId, int horaireId);
}
