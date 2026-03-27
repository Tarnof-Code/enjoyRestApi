package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarnof.enjoyrestapi.enums.EmplacementLieu;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.payload.request.SaveLieuRequest;
import com.tarnof.enjoyrestapi.payload.response.LieuDto;
import com.tarnof.enjoyrestapi.services.LieuService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour LieuController")
@SuppressWarnings("null")
class LieuControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private LieuService lieuService;

    @InjectMocks
    private LieuController lieuController;

    private LieuDto lieuDto;
    private SaveLieuRequest saveLieuRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(lieuController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        lieuDto = new LieuDto(3, "Salle polyvalente", EmplacementLieu.INTERIEUR, 40, 1);
        saveLieuRequest = new SaveLieuRequest("Salle polyvalente", EmplacementLieu.INTERIEUR, 40);
    }

    @Test
    @DisplayName("lister - 200")
    void lister_ShouldReturn200() throws Exception {
        when(lieuService.listerLieuxDuSejour(1)).thenReturn(List.of(lieuDto));

        mockMvc.perform(get("/api/v1/sejours/1/lieux"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].emplacement").value("INTERIEUR"));

        verify(lieuService).listerLieuxDuSejour(1);
    }

    @Test
    @DisplayName("get - 200")
    void get_ShouldReturn200() throws Exception {
        when(lieuService.getLieu(1, 3)).thenReturn(lieuDto);

        mockMvc.perform(get("/api/v1/sejours/1/lieux/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sejourId").value(1))
                .andExpect(jsonPath("$.nombreMax").value(40));

        verify(lieuService).getLieu(1, 3);
    }

    @Test
    @DisplayName("get - 404")
    void get_WhenNotFound_ShouldReturn404() throws Exception {
        when(lieuService.getLieu(1, 99)).thenThrow(new ResourceNotFoundException("Lieu non trouvé"));

        mockMvc.perform(get("/api/v1/sejours/1/lieux/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Lieu non trouvé"));
    }

    @Test
    @DisplayName("creer - 409 si nom déjà utilisé pour le séjour")
    void creer_WhenDuplicateNom_ShouldReturn409() throws Exception {
        when(lieuService.creerLieu(eq(1), any(SaveLieuRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException("Un lieu avec ce nom existe déjà pour ce séjour"));

        mockMvc.perform(post("/api/v1/sejours/1/lieux")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveLieuRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Un lieu avec ce nom existe déjà pour ce séjour"));
    }

    @Test
    @DisplayName("creer - 201")
    void creer_ShouldReturn201() throws Exception {
        when(lieuService.creerLieu(eq(1), any(SaveLieuRequest.class))).thenReturn(lieuDto);

        mockMvc.perform(post("/api/v1/sejours/1/lieux")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveLieuRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));

        verify(lieuService).creerLieu(eq(1), any(SaveLieuRequest.class));
    }

    @Test
    @DisplayName("modifier - 200")
    void modifier_ShouldReturn200() throws Exception {
        when(lieuService.modifierLieu(eq(1), eq(3), any(SaveLieuRequest.class))).thenReturn(lieuDto);

        mockMvc.perform(put("/api/v1/sejours/1/lieux/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveLieuRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Salle polyvalente"));

        verify(lieuService).modifierLieu(eq(1), eq(3), any(SaveLieuRequest.class));
    }

    @Test
    @DisplayName("supprimer - 204")
    void supprimer_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/sejours/1/lieux/3"))
                .andExpect(status().isNoContent());

        verify(lieuService).supprimerLieu(1, 3);
    }
}
