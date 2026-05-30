package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.GenreChambre;
import com.tarnof.enjoyrestapi.enums.TypeChambre;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.payload.request.SaveChambreRequest;
import com.tarnof.enjoyrestapi.payload.response.ChambreDto;
import com.tarnof.enjoyrestapi.services.ChambreService;
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

import java.util.Collections;
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
@DisplayName("Tests unitaires pour ChambreController")
@SuppressWarnings("null")
class ChambreControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ChambreService chambreService;

    @InjectMocks
    private ChambreController chambreController;

    private ChambreDto chambreDto;
    private SaveChambreRequest saveChambreRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(chambreController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        chambreDto = new ChambreDto(
                3,
                1,
                TypeChambre.ENFANT,
                "12",
                "Les filles",
                4,
                GenreChambre.FEMININ,
                "Dortoir filles",
                "A",
                "Est",
                1,
                List.of());
        saveChambreRequest = new SaveChambreRequest(
                TypeChambre.ENFANT,
                "12",
                "Les filles",
                4,
                GenreChambre.FEMININ,
                "Dortoir filles",
                "A",
                "Est",
                1);

        Utilisateur utilisateur = Utilisateur.builder().tokenId("user-token-123").build();
        authentication = new UsernamePasswordAuthenticationToken(
                utilisateur, null, Collections.emptyList());
    }

    @Test
    @DisplayName("lister - 200")
    void lister_ShouldReturn200() throws Exception {
        when(chambreService.listerChambresDuSejour(1, "user-token-123")).thenReturn(List.of(chambreDto));

        mockMvc.perform(get("/api/v1/sejours/1/chambres").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].typeChambre").value("ENFANT"))
                .andExpect(jsonPath("$[0].identifiant").value("12"));

        verify(chambreService).listerChambresDuSejour(1, "user-token-123");
    }

    @Test
    @DisplayName("get - 200")
    void get_ShouldReturn200() throws Exception {
        when(chambreService.getChambre(1, 3, "user-token-123")).thenReturn(chambreDto);

        mockMvc.perform(get("/api/v1/sejours/1/chambres/3").principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sejourId").value(1))
                .andExpect(jsonPath("$.capaciteMax").value(4));

        verify(chambreService).getChambre(1, 3, "user-token-123");
    }

    @Test
    @DisplayName("get - 404")
    void get_WhenNotFound_ShouldReturn404() throws Exception {
        when(chambreService.getChambre(1, 99, "user-token-123"))
                .thenThrow(new ResourceNotFoundException("Chambre non trouvée"));

        mockMvc.perform(get("/api/v1/sejours/1/chambres/99").principal(authentication))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Chambre non trouvée"));
    }

    @Test
    @DisplayName("creer - 409 si identifiant déjà utilisé pour le séjour")
    void creer_WhenDuplicateIdentifiant_ShouldReturn409() throws Exception {
        when(chambreService.creerChambre(eq(1), any(SaveChambreRequest.class)))
                .thenThrow(new ResourceAlreadyExistsException(
                        "Une chambre avec cet identifiant existe déjà pour ce séjour"));

        mockMvc.perform(post("/api/v1/sejours/1/chambres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveChambreRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error")
                        .value("Une chambre avec cet identifiant existe déjà pour ce séjour"));
    }

    @Test
    @DisplayName("creer - 201")
    void creer_ShouldReturn201() throws Exception {
        when(chambreService.creerChambre(eq(1), any(SaveChambreRequest.class))).thenReturn(chambreDto);

        mockMvc.perform(post("/api/v1/sejours/1/chambres")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveChambreRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(3));

        verify(chambreService).creerChambre(eq(1), any(SaveChambreRequest.class));
    }

    @Test
    @DisplayName("modifier - 200")
    void modifier_ShouldReturn200() throws Exception {
        when(chambreService.modifierChambre(eq(1), eq(3), any(SaveChambreRequest.class))).thenReturn(chambreDto);

        mockMvc.perform(put("/api/v1/sejours/1/chambres/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(saveChambreRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identifiant").value("12"))
                .andExpect(jsonPath("$.nom").value("Les filles"));

        verify(chambreService).modifierChambre(eq(1), eq(3), any(SaveChambreRequest.class));
    }

    @Test
    @DisplayName("supprimer - 204")
    void supprimer_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/v1/sejours/1/chambres/3"))
                .andExpect(status().isNoContent());

        verify(chambreService).supprimerChambre(1, 3);
    }
}
