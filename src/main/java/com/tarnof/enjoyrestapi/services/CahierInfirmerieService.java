package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.SaveCahierInfirmerieEntreeRequest;
import com.tarnof.enjoyrestapi.payload.response.CahierInfirmerieEntreeDto;

import java.util.List;

public interface CahierInfirmerieService {

    List<CahierInfirmerieEntreeDto> listerEntreesDuSejour(int sejourId, String utilisateurTokenId);

    CahierInfirmerieEntreeDto getEntree(int sejourId, int entreeId, String utilisateurTokenId);

    CahierInfirmerieEntreeDto creerEntree(int sejourId, SaveCahierInfirmerieEntreeRequest request, String utilisateurTokenId);

    CahierInfirmerieEntreeDto modifierEntree(
            int sejourId, int entreeId, SaveCahierInfirmerieEntreeRequest request, String utilisateurTokenId);

    void supprimerEntree(int sejourId, int entreeId, String utilisateurTokenId);
}
