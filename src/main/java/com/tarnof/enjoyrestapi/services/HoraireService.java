package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.SaveHoraireRequest;
import com.tarnof.enjoyrestapi.payload.response.HoraireDto;

import java.util.List;

public interface HoraireService {
    List<HoraireDto> listerHorairesDuSejour(int sejourId);

    HoraireDto getHoraire(int sejourId, int horaireId);

    HoraireDto creerHoraire(int sejourId, SaveHoraireRequest request);

    HoraireDto modifierHoraire(int sejourId, int horaireId, SaveHoraireRequest request);

    void supprimerHoraire(int sejourId, int horaireId);
}
