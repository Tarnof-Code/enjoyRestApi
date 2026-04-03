package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.enums.EmplacementLieu;
import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.payload.request.CreateActiviteRequest;
import com.tarnof.enjoyrestapi.payload.response.ActiviteDto;
import com.tarnof.enjoyrestapi.payload.response.LieuDto;
import com.tarnof.enjoyrestapi.payload.response.MomentDto;
import com.tarnof.enjoyrestapi.services.ActiviteService;
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

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour ActiviteController")
@SuppressWarnings("null")
class ActiviteControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ActiviteService activiteService;

    @InjectMocks
    private ActiviteController activiteController;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mockMvc = MockMvcBuilders.standaloneSetup(activiteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/sejours/{id}/activites")
    void lister_shouldReturn200() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 5);
        LieuDto lieu = new LieuDto(8, "Terrain", EmplacementLieu.EXTERIEUR, null, false, null, 3);
        MomentDto moment = new MomentDto(2, "Matin", 3, 0);
        List<ActiviteDto.MembreEquipeInfo> membres =
                List.of(new ActiviteDto.MembreEquipeInfo("t1", "N", "P"));
        List<Integer> groupeIds = List.of(2, 4);
        ActiviteDto dto = new ActiviteDto(1, date, "Kayak", "Desc", 3, moment, lieu, membres, groupeIds, null);
        when(activiteService.listerActivitesDuSejour(3)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/sejours/3/activites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nom").value("Kayak"))
                .andExpect(jsonPath("$[0].membres[0].tokenId").value("t1"));
    }

    @Test
    @DisplayName("POST /api/v1/sejours/{id}/activites")
    void creer_shouldReturn201() throws Exception {
        LocalDate date = LocalDate.of(2026, 7, 5);
        Integer lieuId = 7;
        int momentId = 4;
        List<String> membreTokenIds = List.of("tok");
        List<Integer> reqGroupes = List.of(12, 11);
        CreateActiviteRequest req = new CreateActiviteRequest(
                date, "Kayak", null, lieuId, momentId, membreTokenIds, reqGroupes);

        MomentDto momentResponse = new MomentDto(momentId, "Après-midi", 3, 1);
        LieuDto lieuResponse = null;
        List<ActiviteDto.MembreEquipeInfo> membresResponse = Collections.emptyList();
        List<Integer> dtoGroupes = List.of(11, 12);
        ActiviteDto dto = new ActiviteDto(
                9, req.date(), req.nom(), req.description(), 3, momentResponse, lieuResponse, membresResponse, dtoGroupes, null);
        when(activiteService.creerActivite(eq(3), eq(req))).thenReturn(dto);

        mockMvc.perform(post("/api/v1/sejours/3/activites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(9));

        verify(activiteService).creerActivite(3, req);
    }
}
