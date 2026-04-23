package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.enums.PlanningLigneLibelleSource;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.payload.request.SavePlanningGrilleRequest;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleDetailDto;
import com.tarnof.enjoyrestapi.payload.response.PlanningGrilleSummaryDto;
import com.tarnof.enjoyrestapi.services.PlanningGrilleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanningGrilleController")
@SuppressWarnings("null")
class PlanningGrilleControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private PlanningGrilleService planningGrilleService;

    @InjectMocks
    private PlanningGrilleController planningGrilleController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(planningGrilleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("lister - 200")
    void lister_ok() throws Exception {
        when(planningGrilleService.listerGrilles(1))
                .thenReturn(
                        List.of(
                                new PlanningGrilleSummaryDto(
                                        2, 1, "Repas", Instant.parse("2026-07-01T10:00:00Z"))));

        mockMvc.perform(get("/api/v1/sejours/1/planning-grilles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].titre").value("Repas"));

        verify(planningGrilleService).listerGrilles(1);
    }

    @Test
    @DisplayName("get - 404")
    void get_notFound() throws Exception {
        when(planningGrilleService.getGrille(1, 9)).thenThrow(new ResourceNotFoundException("Planning absent"));

        mockMvc.perform(get("/api/v1/sejours/1/planning-grilles/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Planning absent"));
    }

    @Test
    @DisplayName("creer - 201")
    void creer_created() throws Exception {
        var body =
                new SavePlanningGrilleRequest(
                        "Lessive", null, PlanningLigneLibelleSource.SAISIE_LIBRE, null);
        var detail =
                new PlanningGrilleDetailDto(
                        3,
                        1,
                        "Lessive",
                        null,
                        PlanningLigneLibelleSource.SAISIE_LIBRE,
                        PlanningLigneLibelleSource.SAISIE_LIBRE,
                        Instant.parse("2026-07-02T12:00:00Z"),
                        List.of());
        when(planningGrilleService.creerGrille(eq(1), org.mockito.ArgumentMatchers.any(SavePlanningGrilleRequest.class)))
                .thenReturn(detail);

        mockMvc.perform(
                        post("/api/v1/sejours/1/planning-grilles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.titre").value("Lessive"))
                .andExpect(jsonPath("$.sourceLibelleLignes").value("SAISIE_LIBRE"))
                .andExpect(jsonPath("$.sourceContenuCellules").value("SAISIE_LIBRE"));
    }

    @Test
    @DisplayName("creer - 201 sans sourceLibelleLignes dans le JSON")
    void creer_created_sansSourceLibelleLignes() throws Exception {
        var detail =
                new PlanningGrilleDetailDto(
                        4,
                        1,
                        "Repas",
                        null,
                        null,
                        PlanningLigneLibelleSource.SAISIE_LIBRE,
                        Instant.parse("2026-07-02T12:00:00Z"),
                        List.of());
        when(planningGrilleService.creerGrille(eq(1), org.mockito.ArgumentMatchers.any(SavePlanningGrilleRequest.class)))
                .thenReturn(detail);

        mockMvc.perform(
                        post("/api/v1/sejours/1/planning-grilles")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"titre\":\"Repas\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.sourceLibelleLignes").value(org.hamcrest.Matchers.nullValue()));
    }
}
