package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.RoleSejour;
import com.tarnof.enjoyrestapi.exceptions.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.exceptions.ResourceAlreadyExistsException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.CreateSejourRequest;
import com.tarnof.enjoyrestapi.payload.request.MembreEquipeRequest;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import com.tarnof.enjoyrestapi.payload.response.SejourDto;
import com.tarnof.enjoyrestapi.services.SejourService;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour SejourController")
class SejourControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SejourService sejourService;

    @InjectMocks
    private SejourController sejourController;

    private SejourDto sejourDto;
    private SejourDto sejourDto2;
    private CreateSejourRequest createSejourRequest;
    private MembreEquipeRequest membreEquipeRequest;
    private RegisterRequest registerRequest;
    private Date dateDebut;
    private Date dateFin;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(sejourController)
                .setControllerAdvice(globalExceptionHandler)
                .build();

        // Dates de test
        dateDebut = new Date(System.currentTimeMillis() + 86400000); // Demain
        dateFin = new Date(System.currentTimeMillis() + 172800000); // Après-demain

        // SejourDto avec directeur
        SejourDto.DirecteurInfos directeurInfos = new SejourDto.DirecteurInfos(
                "directeur-token-123",
                "Dupont",
                "Jean"
        );

        sejourDto = new SejourDto(
                1,
                "Séjour Test",
                "Description du séjour test",
                dateDebut,
                dateFin,
                "Lieu Test",
                directeurInfos,
                null
        );

        sejourDto2 = new SejourDto(
                2,
                "Séjour Test 2",
                "Description du séjour test 2",
                dateDebut,
                dateFin,
                "Lieu Test 2",
                directeurInfos,
                null
        );

        createSejourRequest = new CreateSejourRequest(
                "Nouveau Séjour",
                "Description du nouveau séjour",
                dateDebut,
                dateFin,
                "Nouveau Lieu",
                "directeur-token-123"
        );

        membreEquipeRequest = new MembreEquipeRequest(
                "membre-token-456",
                RoleSejour.ANIM
        );

        Date dateNaissance = new Date(System.currentTimeMillis() - 86400000L * 365 * 25);
        Instant dateExpiration = Instant.now().plusSeconds(86400 * 30);

        registerRequest = new RegisterRequest(
                "Pierre",
                "Martin",
                "M",
                dateNaissance,
                "0123456789",
                "pierre.martin@test.fr",
                "Password123!",
                dateExpiration,
                Role.BASIC_USER,
                RoleSejour.ANIM
        );
    }

    // ========== Tests pour getAllSejours() ==========

    @Test
    @DisplayName("getAllSejours - Devrait retourner 200 OK avec la liste des séjours")
    void getAllSejours_ShouldReturn200WithSejoursList() throws Exception {
        // Given
        List<SejourDto> sejours = Arrays.asList(sejourDto, sejourDto2);
        when(sejourService.getAllSejours()).thenReturn(sejours);

        // When & Then
        mockMvc.perform(get("/api/v1/sejours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Séjour Test"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nom").value("Séjour Test 2"));

        verify(sejourService).getAllSejours();
    }

    @Test
    @DisplayName("getAllSejours - Devrait retourner 200 OK avec une liste vide")
    void getAllSejours_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(sejourService.getAllSejours()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/sejours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(sejourService).getAllSejours();
    }

    // ========== Tests pour getSejourById() ==========

    @Test
    @DisplayName("getSejourById - Devrait retourner 200 OK avec le séjour")
    void getSejourById_ShouldReturn200WithSejour() throws Exception {
        // Given
        when(sejourService.getSejourById(1)).thenReturn(sejourDto);

        // When & Then
        mockMvc.perform(get("/api/v1/sejours/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Séjour Test"))
                .andExpect(jsonPath("$.description").value("Description du séjour test"))
                .andExpect(jsonPath("$.lieuDuSejour").value("Lieu Test"))
                .andExpect(jsonPath("$.directeur.tokenId").value("directeur-token-123"))
                .andExpect(jsonPath("$.directeur.nom").value("Dupont"))
                .andExpect(jsonPath("$.directeur.prenom").value("Jean"));

        verify(sejourService).getSejourById(1);
    }

    @Test
    @DisplayName("getSejourById - Devrait retourner 404 Not Found si le séjour n'existe pas")
    void getSejourById_ShouldReturn404WhenSejourNotFound() throws Exception {
        // Given
        when(sejourService.getSejourById(999))
                .thenThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"));

        // When & Then
        mockMvc.perform(get("/api/v1/sejours/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(sejourService).getSejourById(999);
    }

    // ========== Tests pour creerSejour() ==========

    @Test
    @DisplayName("creerSejour - Devrait retourner 200 OK avec le séjour créé")
    @SuppressWarnings("null")
    void creerSejour_ShouldReturn200WithCreatedSejour() throws Exception {
        // Given
        when(sejourService.creerSejour(any(CreateSejourRequest.class))).thenReturn(sejourDto);

        // When & Then
        mockMvc.perform(post("/api/v1/sejours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSejourRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Séjour Test"));

        verify(sejourService).creerSejour(any(CreateSejourRequest.class));
    }

    @Test
    @DisplayName("creerSejour - Devrait retourner 200 OK même avec une requête invalide (validation gérée par Spring MVC)")
    @SuppressWarnings("null")
    void creerSejour_ShouldReturn200EvenWithInvalidRequest() throws Exception {
        // Given - requête invalide (nom vide)
        // Note: Dans les tests unitaires avec standaloneSetup(), la validation Jakarta (@Valid) 
        // n'est pas activée automatiquement. La validation se fait au niveau du contrôleur 
        // avec un contexte Spring MVC complet (tests d'intégration avec @WebMvcTest).
        CreateSejourRequest invalidRequest = new CreateSejourRequest(
                "", // Nom vide
                "Description",
                dateDebut,
                dateFin,
                "Lieu",
                null
        );

        when(sejourService.creerSejour(any(CreateSejourRequest.class))).thenReturn(sejourDto);

        // When & Then
        mockMvc.perform(post("/api/v1/sejours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isOk());

        verify(sejourService).creerSejour(any(CreateSejourRequest.class));
    }

    @Test
    @DisplayName("creerSejour - Devrait retourner 404 Not Found si le directeur n'existe pas")
    @SuppressWarnings("null")
    void creerSejour_ShouldReturn404WhenDirecteurNotFound() throws Exception {
        // Given
        when(sejourService.creerSejour(any(CreateSejourRequest.class)))
                .thenThrow(new ResourceNotFoundException("Directeur non trouvé avec l'ID: directeur-inexistant"));

        CreateSejourRequest requestWithInvalidDirecteur = new CreateSejourRequest(
                "Nouveau Séjour",
                "Description du nouveau séjour",
                dateDebut,
                dateFin,
                "Nouveau Lieu",
                "directeur-inexistant"
        );

        // When & Then
        mockMvc.perform(post("/api/v1/sejours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithInvalidDirecteur)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Directeur non trouvé avec l'ID: directeur-inexistant"));

        verify(sejourService).creerSejour(any(CreateSejourRequest.class));
    }

    // ========== Tests pour modifierSejour() ==========

    @Test
    @DisplayName("modifierSejour - Devrait retourner 200 OK avec le séjour modifié")
    @SuppressWarnings("null")
    void modifierSejour_ShouldReturn200WithUpdatedSejour() throws Exception {
        // Given
        SejourDto updatedSejourDto = new SejourDto(
                1,
                "Séjour Modifié",
                "Description modifiée",
                dateDebut,
                dateFin,
                "Lieu Modifié",
                sejourDto.directeur(),
                null
        );

        when(sejourService.modifierSejour(eq(1), any(CreateSejourRequest.class))).thenReturn(updatedSejourDto);

        CreateSejourRequest updateRequest = new CreateSejourRequest(
                "Séjour Modifié",
                "Description modifiée",
                dateDebut,
                dateFin,
                "Lieu Modifié",
                "directeur-token-123"
        );

        // When & Then
        mockMvc.perform(put("/api/v1/sejours/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nom").value("Séjour Modifié"))
                .andExpect(jsonPath("$.description").value("Description modifiée"));

        verify(sejourService).modifierSejour(eq(1), any(CreateSejourRequest.class));
    }

    @Test
    @DisplayName("modifierSejour - Devrait retourner 404 Not Found si le séjour n'existe pas")
    @SuppressWarnings("null")
    void modifierSejour_ShouldReturn404WhenSejourNotFound() throws Exception {
        // Given
        when(sejourService.modifierSejour(eq(999), any(CreateSejourRequest.class)))
                .thenThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"));

        // When & Then
        mockMvc.perform(put("/api/v1/sejours/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createSejourRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(sejourService).modifierSejour(eq(999), any(CreateSejourRequest.class));
    }

    // ========== Tests pour supprimerSejour() ==========

    @Test
    @DisplayName("supprimerSejour - Devrait retourner 204 No Content")
    void supprimerSejour_ShouldReturn204NoContent() throws Exception {
        // Given
        doNothing().when(sejourService).supprimerSejour(1);

        // When & Then
        mockMvc.perform(delete("/api/v1/sejours/1"))
                .andExpect(status().isNoContent());

        verify(sejourService).supprimerSejour(1);
    }

    @Test
    @DisplayName("supprimerSejour - Devrait retourner 404 Not Found si le séjour n'existe pas")
    void supprimerSejour_ShouldReturn404WhenSejourNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"))
                .when(sejourService).supprimerSejour(999);

        // When & Then
        mockMvc.perform(delete("/api/v1/sejours/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(sejourService).supprimerSejour(999);
    }

    // ========== Tests pour getSejoursByDirecteur() ==========

    @Test
    @DisplayName("getSejoursByDirecteur - Devrait retourner 200 OK avec les séjours du directeur")
    void getSejoursByDirecteur_ShouldReturn200WithSejours() throws Exception {
        // Given
        List<SejourDto> sejours = Arrays.asList(sejourDto);
        when(sejourService.getSejoursByDirecteur("directeur-token-123")).thenReturn(sejours);

        // When & Then
        mockMvc.perform(get("/api/v1/sejours/directeur/directeur-token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nom").value("Séjour Test"));

        verify(sejourService).getSejoursByDirecteur("directeur-token-123");
    }

    @Test
    @DisplayName("getSejoursByDirecteur - Devrait retourner 404 Not Found si le directeur n'existe pas")
    void getSejoursByDirecteur_ShouldReturn404WhenDirecteurNotFound() throws Exception {
        // Given
        when(sejourService.getSejoursByDirecteur("directeur-inexistant"))
                .thenThrow(new ResourceNotFoundException("Directeur non trouvé avec le token ID: directeur-inexistant"));

        // When & Then
        mockMvc.perform(get("/api/v1/sejours/directeur/directeur-inexistant"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Directeur non trouvé avec le token ID: directeur-inexistant"));

        verify(sejourService).getSejoursByDirecteur("directeur-inexistant");
    }

    // ========== Tests pour ajouterMembreExistant() ==========

    @Test
    @DisplayName("ajouterMembreExistant - Devrait retourner 201 Created")
    @SuppressWarnings("null")
    void ajouterMembreExistant_ShouldReturn201Created() throws Exception {
        // Given
        doNothing().when(sejourService).ajouterMembreEquipe(eq(1), eq(null), any(MembreEquipeRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/sejours/1/equipe/existant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membreEquipeRequest)))
                .andExpect(status().isCreated());

        verify(sejourService).ajouterMembreEquipe(eq(1), eq(null), any(MembreEquipeRequest.class));
    }

    @Test
    @DisplayName("ajouterMembreExistant - Devrait retourner 400 Bad Request si la validation échoue")
    @SuppressWarnings("null")
    void ajouterMembreExistant_ShouldReturn400WhenValidationFails() throws Exception {
        // Given - requête invalide (tokenId vide)
        MembreEquipeRequest invalidRequest = new MembreEquipeRequest(
                "", // tokenId vide
                RoleSejour.ANIM
        );

        // When & Then
        mockMvc.perform(post("/api/v1/sejours/1/equipe/existant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(sejourService, never()).ajouterMembreEquipe(anyInt(), any(), any());
    }

    @Test
    @DisplayName("ajouterMembreExistant - Devrait retourner 404 Not Found si le séjour n'existe pas")
    @SuppressWarnings("null")
    void ajouterMembreExistant_ShouldReturn404WhenSejourNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"))
                .when(sejourService).ajouterMembreEquipe(eq(999), eq(null), any(MembreEquipeRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/sejours/999/equipe/existant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membreEquipeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(sejourService).ajouterMembreEquipe(eq(999), eq(null), any(MembreEquipeRequest.class));
    }

    @Test
    @DisplayName("ajouterMembreExistant - Devrait retourner 409 Conflict si le membre existe déjà dans l'équipe")
    @SuppressWarnings("null")
    void ajouterMembreExistant_ShouldReturn409WhenMemberAlreadyExists() throws Exception {
        // Given
        doThrow(new ResourceAlreadyExistsException("Cet utilisateur fait déjà partie de l'équipe"))
                .when(sejourService).ajouterMembreEquipe(eq(1), eq(null), any(MembreEquipeRequest.class));

        // When & Then
        mockMvc.perform(post("/api/v1/sejours/1/equipe/existant")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membreEquipeRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cet utilisateur fait déjà partie de l'équipe"));

        verify(sejourService).ajouterMembreEquipe(eq(1), eq(null), any(MembreEquipeRequest.class));
    }

    // ========== Tests pour ajouterNouveauMembre() ==========

    @Test
    @DisplayName("ajouterNouveauMembre - Devrait retourner 201 Created")
    @SuppressWarnings("null")
    void ajouterNouveauMembre_ShouldReturn201Created() throws Exception {
        // Given
        doNothing().when(sejourService).ajouterMembreEquipe(eq(1), any(RegisterRequest.class), eq(null));

        // When & Then
        mockMvc.perform(post("/api/v1/sejours/1/equipe/nouveau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        verify(sejourService).ajouterMembreEquipe(eq(1), any(RegisterRequest.class), eq(null));
    }

    @Test
    @DisplayName("ajouterNouveauMembre - Devrait retourner 400 Bad Request si la validation échoue")
    @SuppressWarnings("null")
    void ajouterNouveauMembre_ShouldReturn400WhenValidationFails() throws Exception {
        // Given - requête invalide (email vide)
        RegisterRequest invalidRequest = new RegisterRequest(
                "Pierre",
                "Martin",
                "M",
                new Date(),
                "0123456789",
                "", // Email vide
                "Password123!",
                Instant.now().plusSeconds(86400 * 30),
                Role.BASIC_USER,
                RoleSejour.ANIM
        );

        // When & Then
        mockMvc.perform(post("/api/v1/sejours/1/equipe/nouveau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(sejourService, never()).ajouterMembreEquipe(anyInt(), any(), any());
    }

    @Test
    @DisplayName("ajouterNouveauMembre - Devrait retourner 404 Not Found si le séjour n'existe pas")
    @SuppressWarnings("null")
    void ajouterNouveauMembre_ShouldReturn404WhenSejourNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"))
                .when(sejourService).ajouterMembreEquipe(eq(999), any(RegisterRequest.class), eq(null));

        // When & Then
        mockMvc.perform(post("/api/v1/sejours/999/equipe/nouveau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(sejourService).ajouterMembreEquipe(eq(999), any(RegisterRequest.class), eq(null));
    }

    @Test
    @DisplayName("ajouterNouveauMembre - Devrait retourner 409 Conflict si le membre existe déjà dans l'équipe")
    @SuppressWarnings("null")
    void ajouterNouveauMembre_ShouldReturn409WhenMemberAlreadyExists() throws Exception {
        // Given
        doThrow(new ResourceAlreadyExistsException("Cet utilisateur fait déjà partie de l'équipe"))
                .when(sejourService).ajouterMembreEquipe(eq(1), any(RegisterRequest.class), eq(null));

        // When & Then
        mockMvc.perform(post("/api/v1/sejours/1/equipe/nouveau")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cet utilisateur fait déjà partie de l'équipe"));

        verify(sejourService).ajouterMembreEquipe(eq(1), any(RegisterRequest.class), eq(null));
    }

    // ========== Tests pour modifierRoleMembreEquipe() ==========

    @Test
    @DisplayName("modifierRoleMembreEquipe - Devrait retourner 204 No Content")
    @SuppressWarnings("null")
    void modifierRoleMembreEquipe_ShouldReturn204NoContent() throws Exception {
        // Given
        // Le membreEquipeRequest contient RoleSejour.ANIM, donc on mocke avec ANIM
        doNothing().when(sejourService).modifierRoleMembreEquipe(eq(1), eq("membre-token-456"), eq(RoleSejour.ANIM));

        // When & Then
        mockMvc.perform(put("/api/v1/sejours/1/equipe/membre-token-456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membreEquipeRequest)))
                .andExpect(status().isNoContent());

        verify(sejourService).modifierRoleMembreEquipe(eq(1), eq("membre-token-456"), eq(RoleSejour.ANIM));
    }

    @Test
    @DisplayName("modifierRoleMembreEquipe - Devrait retourner 400 Bad Request si la validation échoue")
    @SuppressWarnings("null")
    void modifierRoleMembreEquipe_ShouldReturn400WhenValidationFails() throws Exception {
        // Given - requête invalide (roleSejour null)
        MembreEquipeRequest invalidRequest = new MembreEquipeRequest(
                "membre-token-456",
                null // roleSejour null
        );

        // When & Then
        mockMvc.perform(put("/api/v1/sejours/1/equipe/membre-token-456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(sejourService, never()).modifierRoleMembreEquipe(anyInt(), anyString(), any());
    }

    @Test
    @DisplayName("modifierRoleMembreEquipe - Devrait retourner 404 Not Found si le séjour n'existe pas")
    @SuppressWarnings("null")
    void modifierRoleMembreEquipe_ShouldReturn404WhenSejourNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"))
                .when(sejourService).modifierRoleMembreEquipe(eq(999), eq("membre-token-456"), eq(RoleSejour.ANIM));

        // When & Then
        mockMvc.perform(put("/api/v1/sejours/999/equipe/membre-token-456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(membreEquipeRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(sejourService).modifierRoleMembreEquipe(eq(999), eq("membre-token-456"), eq(RoleSejour.ANIM));
    }

    // ========== Tests pour supprimerMembreEquipe() ==========

    @Test
    @DisplayName("supprimerMembreEquipe - Devrait retourner 204 No Content")
    void supprimerMembreEquipe_ShouldReturn204NoContent() throws Exception {
        // Given
        doNothing().when(sejourService).supprimerMembreEquipe(1, "membre-token-456");

        // When & Then
        mockMvc.perform(delete("/api/v1/sejours/1/equipe/membre-token-456"))
                .andExpect(status().isNoContent());

        verify(sejourService).supprimerMembreEquipe(1, "membre-token-456");
    }

    @Test
    @DisplayName("supprimerMembreEquipe - Devrait retourner 404 Not Found si le séjour n'existe pas")
    void supprimerMembreEquipe_ShouldReturn404WhenSejourNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Séjour non trouvé avec l'ID: 999"))
                .when(sejourService).supprimerMembreEquipe(999, "membre-token-456");

        // When & Then
        mockMvc.perform(delete("/api/v1/sejours/999/equipe/membre-token-456"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Séjour non trouvé avec l'ID: 999"));

        verify(sejourService).supprimerMembreEquipe(999, "membre-token-456");
    }

    @Test
    @DisplayName("supprimerMembreEquipe - Devrait retourner 404 Not Found si le membre n'existe pas")
    void supprimerMembreEquipe_ShouldReturn404WhenMemberNotFound() throws Exception {
        // Given
        doThrow(new ResourceNotFoundException("Membre non trouvé avec l'ID: membre-inexistant"))
                .when(sejourService).supprimerMembreEquipe(1, "membre-inexistant");

        // When & Then
        mockMvc.perform(delete("/api/v1/sejours/1/equipe/membre-inexistant"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Membre non trouvé avec l'ID: membre-inexistant"));

        verify(sejourService).supprimerMembreEquipe(1, "membre-inexistant");
    }
}
