package com.tarnof.enjoyrestapi.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarnof.enjoyrestapi.entities.Reunion;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveReunionRequest;
import com.tarnof.enjoyrestapi.payload.response.ReunionDto;
import com.tarnof.enjoyrestapi.repositories.ReunionRepository;
import com.tarnof.enjoyrestapi.services.ReunionService;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class ReunionServiceImpl implements ReunionService {

    private static final JsonNode DOCUMENT_VIDE_PAR_DEFAUT = defaultDocumentVide();

    private final ReunionRepository reunionRepository;
    private final SejourVerificationService sejourVerificationService;
    private final ObjectMapper objectMapper;

    public ReunionServiceImpl(
            ReunionRepository reunionRepository,
            SejourVerificationService sejourVerificationService,
            ObjectMapper objectMapper) {
        this.reunionRepository = reunionRepository;
        this.sejourVerificationService = sejourVerificationService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReunionDto> listerReunionsDuSejour(int sejourId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        return reunionRepository.findBySejour_IdOrderByDateReunionAscIdAsc(sejourId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReunionDto getReunion(int sejourId, int reunionId, String utilisateurTokenId) {
        sejourVerificationService.verifierAppartenanceAuSejour(sejourId, utilisateurTokenId);
        Reunion reunion = reunionRepository
                .findByIdAndSejour_Id(reunionId, sejourId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Réunion non trouvée avec l'ID: " + reunionId + " pour ce séjour"));
        return mapToDto(reunion);
    }

    @Override
    @Transactional
    public ReunionDto creerReunion(int sejourId, SaveReunionRequest request) {
        Sejour sejour = sejourVerificationService.verifierSejourExiste(sejourId);
        Reunion reunion = new Reunion();
        reunion.setSejour(sejour);
        reunion.setDateReunion(request.date());
        reunion.setOrdreDuJour(normaliserOrdreDuJour(request.ordreDuJour()));
        reunion.setContenuJson(serializeContenu(request.contenu()));
        return mapToDto(reunionRepository.save(reunion));
    }

    @Override
    @Transactional
    public ReunionDto modifierReunion(int sejourId, int reunionId, SaveReunionRequest request) {
        Reunion reunion = reunionRepository
                .findByIdAndSejour_Id(reunionId, sejourId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Réunion non trouvée avec l'ID: " + reunionId + " pour ce séjour"));
        reunion.setDateReunion(request.date());
        reunion.setOrdreDuJour(normaliserOrdreDuJour(request.ordreDuJour()));
        reunion.setContenuJson(serializeContenu(request.contenu()));
        return mapToDto(reunionRepository.save(reunion));
    }

    @Override
    @Transactional
    public void supprimerReunion(int sejourId, int reunionId) {
        Reunion reunion = reunionRepository
                .findByIdAndSejour_Id(reunionId, sejourId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Réunion non trouvée avec l'ID: " + reunionId + " pour ce séjour"));
        reunionRepository.delete(reunion);
    }

    private static String normaliserOrdreDuJour(String ordreDuJour) {
        if (ordreDuJour == null || ordreDuJour.isBlank()) {
            return null;
        }
        return ordreDuJour.trim();
    }

    private ReunionDto mapToDto(Reunion reunion) {
        return new ReunionDto(
                reunion.getId(),
                reunion.getSejour().getId(),
                reunion.getDateReunion(),
                reunion.getOrdreDuJour(),
                parseContenu(reunion.getContenuJson()));
    }

    private String serializeContenu(JsonNode contenu) {
        try {
            return objectMapper.writeValueAsString(contenu);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Contenu invalide : impossible à sérialiser en JSON.", e);
        }
    }

    private JsonNode parseContenu(String json) {
        if (json == null || json.isBlank()) {
            return DOCUMENT_VIDE_PAR_DEFAUT;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Contenu JSON en base illisible pour la réunion.", e);
        }
    }

    private static JsonNode defaultDocumentVide() {
        try {
            return new ObjectMapper().readTree("{\"type\":\"doc\",\"content\":[]}");
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Impossible de construire le document vide par défaut.", e);
        }
    }
}
