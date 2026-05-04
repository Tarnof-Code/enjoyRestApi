package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.enums.TypeReferenceAlimentaire;
import com.tarnof.enjoyrestapi.payload.request.SaveReferenceAlimentaireRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateReferenceAlimentaireRequest;
import com.tarnof.enjoyrestapi.payload.response.ReferenceAlimentaireDto;

import java.util.List;

public interface ReferenceAlimentaireService {

    List<ReferenceAlimentaireDto> lister(TypeReferenceAlimentaire type);

    ReferenceAlimentaireDto getById(int id);

    ReferenceAlimentaireDto creer(SaveReferenceAlimentaireRequest request);

    ReferenceAlimentaireDto modifier(int id, UpdateReferenceAlimentaireRequest request);

    void supprimer(int id);
}
