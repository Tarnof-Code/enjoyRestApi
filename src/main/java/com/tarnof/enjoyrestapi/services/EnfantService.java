package com.tarnof.enjoyrestapi.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.tarnof.enjoyrestapi.payload.response.DossierEnfantDto;
import com.tarnof.enjoyrestapi.payload.response.EnfantDto;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportResponse;
import com.tarnof.enjoyrestapi.payload.request.CreateEnfantRequest;

public interface EnfantService {
    void creerEtAjouterEnfantAuSejour(int sejourId, CreateEnfantRequest request);
    EnfantDto modifierEnfant(int sejourId, int enfantId, CreateEnfantRequest request);
    void supprimerEnfantDuSejour(int sejourId, int enfantId);
    void supprimerTousLesEnfantsDuSejour(int sejourId);
    List<EnfantDto> getEnfantsDuSejour(int sejourId);
    DossierEnfantDto getDossierEnfant(int sejourId, int enfantId, String utilisateurTokenId);
    ExcelImportResponse importerEnfantsDepuisExcel(int sejourId, MultipartFile file);
}
