package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.payload.request.SaveReunionRequest;
import com.tarnof.enjoyrestapi.payload.response.ReunionDto;
import com.tarnof.enjoyrestapi.services.ReunionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour ReunionController")
@SuppressWarnings("null")
class ReunionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ReunionService reunionService;

    @InjectMocks
    private ReunionController reunionController;

    private Authentication authentication;

    private JsonNode contenuTipTap;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        mockMvc = MockMvcBuilders.standaloneSetup(reunionController)
                .setControllerAdvice(globalExceptionHandler)
                .build();

        Utilisateur utilisateur = Utilisateur.builder().tokenId("tok-abc").build();
        authentication =
                new UsernamePasswordAuthenticationToken(utilisateur, null, List.of());

        contenuTipTap =
                objectMapper.readTree(
                        "{\"type\":\"doc\",\"content\":[{\"type\":\"paragraph\",\"content\":[]}]}");
    }

    @Test
    @DisplayName("GET liste delegue au service avec verification sejour via token")
    void lister_delegates() throws Exception {
        LocalDate jour = LocalDate.of(2026, 6, 1);
        ReunionDto dto =
                new ReunionDto(1, 10, jour, "Point A — Point B", contenuTipTap);
        when(reunionService.listerReunionsDuSejour(eq(10), eq("tok-abc"))).thenReturn(List.of(dto));

        mockMvc.perform(
                        get("/api/v1/sejours/10/reunions")
                                .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].sejourId").value(10))
                .andExpect(jsonPath("$[0].date").value("2026-06-01"))
                .andExpect(jsonPath("$[0].ordreDuJour").value("Point A — Point B"));
    }

    @Test
    @DisplayName("POST cree une reunion et retourne 201")
    void creer_retourCreated() throws Exception {
        SaveReunionRequest req =
                new SaveReunionRequest(LocalDate.of(2026, 5, 20), "ODJ test", contenuTipTap);
        ReunionDto created =
                new ReunionDto(3, 2, LocalDate.of(2026, 5, 20), "ODJ test", contenuTipTap);

        when(reunionService.creerReunion(eq(2), any(SaveReunionRequest.class))).thenReturn(created);

        mockMvc.perform(
                        post("/api/v1/sejours/2/reunions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.date").value("2026-05-20"));
    }
}
