package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.CreateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.ActiviteDto;

import java.util.List;

public interface ActiviteService {

    List<ActiviteDto> listerActivitesDuSejour(int sejourId);

    ActiviteDto getActivite(int sejourId, int activiteId);

    ActiviteDto creerActivite(int sejourId, CreateActiviteRequest request);

    ActiviteDto modifierActivite(int sejourId, int activiteId, UpdateActiviteRequest request);

    void supprimerActivite(int sejourId, int activiteId);
}
