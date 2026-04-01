package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.SaveMomentRequest;
import com.tarnof.enjoyrestapi.payload.response.MomentDto;

import java.util.List;

public interface MomentService {

    List<MomentDto> listerMomentsDuSejour(int sejourId);

    MomentDto getMoment(int sejourId, int momentId);

    MomentDto creerMoment(int sejourId, SaveMomentRequest request);

    MomentDto modifierMoment(int sejourId, int momentId, SaveMomentRequest request);

    void supprimerMoment(int sejourId, int momentId);
}
