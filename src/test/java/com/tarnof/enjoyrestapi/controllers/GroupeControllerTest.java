package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.enums.TypeGroupe;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.payload.request.AjouterReferentRequest;
import com.tarnof.enjoyrestapi.payload.request.CreateGroupeRequest;
import com.tarnof.enjoyrestapi.payload.response.GroupeDto;
import com.tarnof.enjoyrestapi.services.GroupeService;
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

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour GroupeController")
@SuppressWarnings("null")
class GroupeControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private GroupeService groupeService;

    @InjectMocks
    private GroupeController groupeController;

    private GroupeDto groupeDto;
    private CreateGroupeRequest createGroupeRequest;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(groupeController)
                .setControllerAdvice(globalExceptionHandler)
                .build();

        groupeDto = new GroupeDto(
                5,
                "Groupe A",
                "Description",
                TypeGroupe.THEMATIQUE,
                null,
                null,
                null,
                null,
                1,
                Collections.emptyList(),
                Collections.emptyList()
        );

        createGroupeRequest = new CreateGroupeRequest(
                "Nouveau groupe",
                "Une description",
                TypeGroupe.THEMATIQUE,
                null,
                null,
                null,
                null
        );
    }

    @Test
    @DisplayName("getGroupesDuSejour - 200 avec liste")
    void getGroupesDuSejour_ShouldReturn200() throws Exception {
        when(groupeService.getGroupesDuSejour(1)).thenReturn(List.of(groupeDto));

        mockMvc.perform(get("/api/v1/sejours/1/groupes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].nom").value("Groupe A"));

        verify(groupeService).getGroupesDuSejour(1);
    }

    @Test
    @DisplayName("getGroupeById - 200")
    void getGroupeById_ShouldReturn200() throws Exception {
        when(groupeService.getGroupeById(1, 5)).thenReturn(groupeDto);

        mockMvc.perform(get("/api/v1/sejours/1/groupes/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.sejourId").value(1));

        verify(groupeService).getGroupeById(1, 5);
    }

    @Test
    @DisplayName("getGroupeById - 404 si service lève ResourceNotFoundException")
    void getGroupeById_WhenNotFound_ShouldReturn404() throws Exception {
        when(groupeService.getGroupeById(1, 99))
                .thenThrow(new ResourceNotFoundException("Groupe non trouvé"));

        mockMvc.perform(get("/api/v1/sejours/1/groupes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Groupe non trouvé"));
    }

    @Test
    @DisplayName("creerGroupe - 201 Created")
    void creerGroupe_ShouldReturn201() throws Exception {
        when(groupeService.creerGroupe(eq(1), any(CreateGroupeRequest.class))).thenReturn(groupeDto);

        mockMvc.perform(post("/api/v1/sejours/1/groupes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nom").value("Groupe A"));

        verify(groupeService).creerGroupe(eq(1), any(CreateGroupeRequest.class));
    }

    @Test
    @DisplayName("modifierGroupe - 200 OK")
    void modifierGroupe_ShouldReturn200() throws Exception {
        when(groupeService.modifierGroupe(eq(1), eq(5), any(CreateGroupeRequest.class))).thenReturn(groupeDto);

        mockMvc.perform(put("/api/v1/sejours/1/groupes/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createGroupeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(groupeService).modifierGroupe(eq(1), eq(5), any(CreateGroupeRequest.class));
    }

    @Test
    @DisplayName("supprimerGroupe - 204 No Content")
    void supprimerGroupe_ShouldReturn204() throws Exception {
        doNothing().when(groupeService).supprimerGroupe(1, 5);

        mockMvc.perform(delete("/api/v1/sejours/1/groupes/5"))
                .andExpect(status().isNoContent());

        verify(groupeService).supprimerGroupe(1, 5);
    }

    @Test
    @DisplayName("ajouterEnfantAuGroupe - 204")
    void ajouterEnfantAuGroupe_ShouldReturn204() throws Exception {
        doNothing().when(groupeService).ajouterEnfantAuGroupe(1, 5, 12);

        mockMvc.perform(post("/api/v1/sejours/1/groupes/5/enfants/12"))
                .andExpect(status().isNoContent());

        verify(groupeService).ajouterEnfantAuGroupe(1, 5, 12);
    }

    @Test
    @DisplayName("ajouterEnfantAuGroupe - 409 si déjà dans le groupe")
    void ajouterEnfantAuGroupe_whenConflict_ShouldReturn409() throws Exception {
        doThrow(new ResourceAlreadyExistsException("Cet enfant fait déjà partie du groupe"))
                .when(groupeService).ajouterEnfantAuGroupe(1, 5, 12);

        mockMvc.perform(post("/api/v1/sejours/1/groupes/5/enfants/12"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cet enfant fait déjà partie du groupe"));
    }

    @Test
    @DisplayName("retirerEnfantDuGroupe - 204")
    void retirerEnfantDuGroupe_ShouldReturn204() throws Exception {
        doNothing().when(groupeService).retirerEnfantDuGroupe(1, 5, 12);

        mockMvc.perform(delete("/api/v1/sejours/1/groupes/5/enfants/12"))
                .andExpect(status().isNoContent());

        verify(groupeService).retirerEnfantDuGroupe(1, 5, 12);
    }

    @Test
    @DisplayName("ajouterReferent - 201 Created")
    void ajouterReferent_ShouldReturn201() throws Exception {
        doNothing().when(groupeService).ajouterReferent(eq(1), eq(5), any(AjouterReferentRequest.class));

        mockMvc.perform(post("/api/v1/sejours/1/groupes/5/referents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AjouterReferentRequest("tok-r1"))))
                .andExpect(status().isCreated());

        verify(groupeService).ajouterReferent(eq(1), eq(5), any(AjouterReferentRequest.class));
    }

    @Test
    @DisplayName("retirerReferent - 204")
    void retirerReferent_ShouldReturn204() throws Exception {
        doNothing().when(groupeService).retirerReferent(1, 5, "tok-r1");

        mockMvc.perform(delete("/api/v1/sejours/1/groupes/5/referents/tok-r1"))
                .andExpect(status().isNoContent());

        verify(groupeService).retirerReferent(1, 5, "tok-r1");
    }
}
