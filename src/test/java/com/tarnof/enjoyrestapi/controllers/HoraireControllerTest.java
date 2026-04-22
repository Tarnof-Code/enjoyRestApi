package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.payload.request.SaveHoraireRequest;
import com.tarnof.enjoyrestapi.payload.response.HoraireDto;
import com.tarnof.enjoyrestapi.services.HoraireService;
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
@DisplayName("Tests unitaires pour HoraireController")
@SuppressWarnings("null")
class HoraireControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private HoraireService horaireService;

    @InjectMocks
    private HoraireController horaireController;

    private HoraireDto horaireDto;
    private SaveHoraireRequest saveHoraireRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(horaireController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        horaireDto = new HoraireDto(3, "8h30", 1);
        saveHoraireRequest = new SaveHoraireRequest("8h30");
    }

    @Test
    @DisplayName("lister - 200")
    void lister_ShouldReturn200() throws Exception {
        when(horaireService.listerHorairesDuSejour(1)).thenReturn(List.of(horaireDto));

        mockMvc.perform(get("/api/v1/sejours/1/horaires"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].libelle").value("8h30"));

        verify(horaireService).listerHorairesDuSejour(1);
    }

    @Test
    @DisplayName("get - 200")
    void get_ShouldReturn200() throws Exception {
        when(horaireService.getHoraire(1, 3)).thenReturn(horaireDto);

        mockMvc.perform(get("/api/v1/sejours/1/horaires/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sejourId").value(1))
                .andExpect(jsonPath("$.libelle").value("8h30"));

        verify(horaireService).getHoraire(1, 3);
    }

    @Test
    @DisplayName("get - 404")
    void get_WhenNotFound_ShouldReturn404() throws Exception {
        when(horaireService.getHoraire(1, 99)).thenThrow(new ResourceNotFoundException("Horaire non trouvé"));

        mockMvc.perform(get("/api/v1/sejours/1/horaires/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Horaire non trouvé"));
    }

    @Test
    @DisplayName("creer - 409 si libellé déjà utilisé pour le séjour")
    void creer_WhenDuplicateLibelle_ShouldReturn409() throws Exception {
        when(horaireService.creerHoraire(eq(1), any(SaveHoraireRequest.class)))
                .thenThrow(
                        new ResourceAlreadyExistsException(
                                "Un horaire avec ce libellé existe déjà pour ce séjour"));

        mockMvc.perform(post("/api/v1/sejours/1/horaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveHoraireRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Un horaire avec ce libellé existe déjà pour ce séjour"));
    }

    @Test
    @DisplayName("creer - 201")
    void creer_ShouldReturn201() throws Exception {
        when(horaireService.creerHoraire(eq(1), any(SaveHoraireRequest.class))).thenReturn(horaireDto);

        mockMvc.perform(post("/api/v1/sejours/1/horaires")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveHoraireRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));

        verify(horaireService).creerHoraire(eq(1), any(SaveHoraireRequest.class));
    }

    @Test
    @DisplayName("modifier - 200")
    void modifier_ShouldReturn200() throws Exception {
        when(horaireService.modifierHoraire(eq(1), eq(3), any(SaveHoraireRequest.class)))
                .thenReturn(horaireDto);

        mockMvc.perform(put("/api/v1/sejours/1/horaires/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveHoraireRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.libelle").value("8h30"));

        verify(horaireService).modifierHoraire(eq(1), eq(3), any(SaveHoraireRequest.class));
    }

    @Test
    @DisplayName("supprimer - 204")
    void supprimer_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/sejours/1/horaires/3"))
                .andExpect(status().isNoContent());

        verify(horaireService).supprimerHoraire(1, 3);
    }
}
