package com.tarnof.enjoyrestapi.services.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.entities.Reunion;
import com.tarnof.enjoyrestapi.entities.Sejour;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.SaveReunionRequest;
import com.tarnof.enjoyrestapi.payload.response.ReunionDto;
import com.tarnof.enjoyrestapi.repositories.ReunionRepository;
import com.tarnof.enjoyrestapi.services.SejourVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReunionServiceImpl")
@SuppressWarnings("null")
class ReunionServiceImplTest {

    @Mock
    private ReunionRepository reunionRepository;

    @Mock
    private SejourVerificationService sejourVerificationService;

    private ReunionServiceImpl reunionService;

    private ObjectMapper objectMapper;

    private JsonNode contenu;

    private Sejour sejour;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        reunionService =
                new ReunionServiceImpl(reunionRepository, sejourVerificationService, objectMapper);

        contenu = objectMapper.readTree("{\"type\":\"doc\",\"content\":[]}");

        sejour = new Sejour();
        sejour.setId(7);
    }

    @Test
    @DisplayName("creer serialise le JSON TipTap en base et associe au sejour")
    void creer_serializeEtAssocieSejour() {
        when(sejourVerificationService.verifierSejourExiste(7)).thenReturn(sejour);
        when(reunionRepository.save(any(Reunion.class)))
                .thenAnswer(invocation -> {
                    Reunion r = invocation.getArgument(0);
                    if (r.getId() == null) {
                        r.setId(100);
                    }
                    return r;
                });

        SaveReunionRequest req =
                new SaveReunionRequest(LocalDate.of(2026, 4, 1), " Réunion projet ", contenu);
        reunionService.creerReunion(7, req);

        ArgumentCaptor<Reunion> captor = ArgumentCaptor.forClass(Reunion.class);
        verify(reunionRepository).save(captor.capture());
        Reunion saved = captor.getValue();

        assertThat(saved.getOrdreDuJour()).isEqualTo("Réunion projet");
        assertThat(saved.getDateReunion()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(saved.getContenuJson()).isEqualTo("{\"type\":\"doc\",\"content\":[]}");
        assertThat(saved.getSejour().getId()).isEqualTo(7);
    }

    @Test
    @DisplayName("get reunion absente pour ce sejour leve ResourceNotFoundException")
    void getAbsentLeve404() {
        when(reunionRepository.findByIdAndSejour_Id(5, 7)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reunionService.getReunion(7, 5, "tok"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("liste appelle verification appartenance au sejour")
    void listeVerifAppartenance() {
        when(reunionRepository.findBySejour_IdOrderByDateReunionAscIdAsc(7)).thenReturn(List.of());
        reunionService.listerReunionsDuSejour(7, "tok");
        verify(sejourVerificationService).verifierAppartenanceAuSejour(7, "tok");
    }

    @Test
    @DisplayName("dto retour parse le JSON persisté")
    void mapVersDtoLitJson() {
        Reunion r = new Reunion();
        r.setId(9);
        r.setOrdreDuJour("Pts");
        r.setDateReunion(LocalDate.of(2026, 1, 2));
        r.setSejour(sejour);
        r.setContenuJson("{\"type\":\"doc\",\"content\":[{\"type\":\"heading\",\"attrs\":{\"level\":2}}]}");

        when(reunionRepository.findByIdAndSejour_Id(9, 7)).thenReturn(Optional.of(r));

        ReunionDto dto = reunionService.getReunion(7, 9, "tok");
        verify(sejourVerificationService).verifierAppartenanceAuSejour(7, "tok");

        assertThat(dto.id()).isEqualTo(9);
        assertThat(dto.contenu().get("type").asText()).isEqualTo("doc");
        assertThat(dto.contenu().path("content").get(0).path("type").asText()).isEqualTo("heading");
    }
}
