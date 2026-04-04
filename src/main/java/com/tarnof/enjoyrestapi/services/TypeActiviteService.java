package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.SaveTypeActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.TypeActiviteDto;

import java.util.List;

public interface TypeActiviteService {

    /** Crée les types par défaut pour ce séjour s'ils manquent ; marque {@code predefini} pour les libellés connus. */
    void assurerTypesParDefautPourSejour(int sejourId);

    List<TypeActiviteDto> listerTypesActivite(int sejourId);

    TypeActiviteDto getTypeActivite(int sejourId, int id);

    TypeActiviteDto creerTypeActivite(int sejourId, SaveTypeActiviteRequest request);

    TypeActiviteDto modifierTypeActivite(int sejourId, int id, SaveTypeActiviteRequest request);

    void supprimerTypeActivite(int sejourId, int id);
}
