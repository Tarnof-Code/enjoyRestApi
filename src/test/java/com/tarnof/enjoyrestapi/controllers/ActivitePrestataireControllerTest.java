package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationAction;
import com.tarnof.enjoyrestapi.enums.HistoriqueModificationType;
import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationActivitePrestataireDto;
import com.tarnof.enjoyrestapi.payload.response.HistoriqueModificationBaseDto;
import com.tarnof.enjoyrestapi.services.ActivitePrestataireService;
import com.tarnof.enjoyrestapi.services.HistoriqueModificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActivitePrestataireController")
@SuppressWarnings("null")
class ActivitePrestataireControllerTest {

    @Mock
    private ActivitePrestataireService activitePrestataireService;
    @Mock
    private HistoriqueModificationService historiqueModificationService;

    @InjectMocks
    private ActivitePrestataireController activitePrestataireController;

    private MockMvc mockMvc;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(activitePrestataireController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        Utilisateur utilisateur = Utilisateur.builder().tokenId("user-token-123").build();
        authentication = new UsernamePasswordAuthenticationToken(
                utilisateur, null, Collections.emptyList());
    }

    @Test
    @DisplayName("GET historique - 200")
    void historique_ShouldReturn200() throws Exception {
        HistoriqueModificationBaseDto base = new HistoriqueModificationBaseDto(
                1,
                HistoriqueModificationType.ACTIVITE_PRESTATAIRE,
                Instant.parse("2026-07-01T10:00:00Z"),
                "mod-token",
                "Dupont",
                "Jean",
                HistoriqueModificationAction.CREATION,
                null,
                "Nom: Kayak | Date: 2026-07-05");
        HistoriqueModificationActivitePrestataireDto dto =
                new HistoriqueModificationActivitePrestataireDto(base, 7);

        when(historiqueModificationService.listerHistoriqueActivitePrestataire(10, 7, "user-token-123"))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/sejours/10/activites-prestataires/7/historique")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].type").value("ACTIVITE_PRESTATAIRE"))
                .andExpect(jsonPath("$[0].activitePrestataireId").value(7))
                .andExpect(jsonPath("$[0].action").value("CREATION"));

        verify(historiqueModificationService).listerHistoriqueActivitePrestataire(10, 7, "user-token-123");
    }

    @Test
    @DisplayName("GET historique - 404")
    void historique_WhenNotFound_ShouldReturn404() throws Exception {
        when(historiqueModificationService.listerHistoriqueActivitePrestataire(10, 99, "user-token-123"))
                .thenThrow(new ResourceNotFoundException("Activité prestataire non trouvée"));

        mockMvc.perform(get("/api/v1/sejours/10/activites-prestataires/99/historique")
                        .principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Activité prestataire non trouvée"));
    }
}
