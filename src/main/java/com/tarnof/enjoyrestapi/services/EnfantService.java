package com.tarnof.enjoyrestapi.services;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.tarnof.enjoyrestapi.payload.response.DossierEnfantDto;
import com.tarnof.enjoyrestapi.payload.response.EnfantDossierSanitaireLigneDto;
import com.tarnof.enjoyrestapi.payload.response.EnfantDto;
import com.tarnof.enjoyrestapi.payload.response.ExcelImportResponse;
import com.tarnof.enjoyrestapi.payload.request.CreateEnfantRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateDossierEnfantRequest;

public interface EnfantService {
    void creerEtAjouterEnfantAuSejour(int sejourId, CreateEnfantRequest request, String utilisateurTokenId);
    EnfantDto modifierEnfant(int sejourId, int enfantId, CreateEnfantRequest request, String utilisateurTokenId);
    void supprimerEnfantDuSejour(int sejourId, int enfantId, String utilisateurTokenId);
    void supprimerTousLesEnfantsDuSejour(int sejourId, String utilisateurTokenId);
    List<EnfantDto> getEnfantsDuSejour(int sejourId, String utilisateurTokenId);
    List<EnfantDossierSanitaireLigneDto> listerDossiersEnfantsDuSejour(int sejourId, String utilisateurTokenId);
    DossierEnfantDto getDossierEnfant(int sejourId, int enfantId, String utilisateurTokenId);
    DossierEnfantDto modifierDossierEnfant(int sejourId, int enfantId, UpdateDossierEnfantRequest request, String utilisateurTokenId);
    ExcelImportResponse importerEnfantsDepuisExcel(int sejourId, MultipartFile file, String utilisateurTokenId);
}
