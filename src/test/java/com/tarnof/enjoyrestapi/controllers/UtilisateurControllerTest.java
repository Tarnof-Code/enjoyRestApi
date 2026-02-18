package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.payload.request.ChangePasswordRequest;
import com.tarnof.enjoyrestapi.payload.request.UpdateUserRequest;
import com.tarnof.enjoyrestapi.payload.response.ProfilDto;
import com.tarnof.enjoyrestapi.services.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Tests unitaires pour UtilisateurController")
class UtilisateurControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UtilisateurService utilisateurService;

    @InjectMocks
    private UtilisateurController utilisateurController;

    private Utilisateur utilisateur;
    private ProfilDto profilDto;
    private UpdateUserRequest updateUserRequest;
    private ChangePasswordRequest changePasswordRequest;
    private Authentication authenticationAdmin;
    private Authentication authenticationUser;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(utilisateurController)
                .setControllerAdvice(globalExceptionHandler)
                .build();

        Date dateNaissance = new Date(System.currentTimeMillis() - 86400000L * 365 * 25);
        Instant dateExpiration = Instant.now().plusSeconds(86400 * 30);

        utilisateur = Utilisateur.builder()
                .id(1)
                .tokenId("token-123")
                .role(Role.BASIC_USER)
                .nom("Dupont")
                .prenom("Jean")
                .genre(Genre.Masculin)
                .email("jean.dupont@test.fr")
                .telephone("0123456789")
                .dateNaissance(dateNaissance)
                .build();

        profilDto = new ProfilDto(
                "token-123",
                Role.BASIC_USER,
                null,
                "Dupont",
                "Jean",
                Genre.Masculin,
                "jean.dupont@test.fr",
                "0123456789",
                dateNaissance,
                dateExpiration
        );

        updateUserRequest = new UpdateUserRequest(
                "token-123",
                "Jean",
                "Dupont",
                "M",
                "jean.dupont@test.fr",
                "0123456789",
                dateNaissance,
                Role.BASIC_USER,
                dateExpiration
        );

        changePasswordRequest = new ChangePasswordRequest(
                "token-123",
                "OldPassword123!",
                "NewPassword123!"
        );

        // Mock Authentication avec droits admin
        authenticationAdmin = mock(Authentication.class);
        GrantedAuthority adminAuthority = mock(GrantedAuthority.class);
        when(adminAuthority.getAuthority()).thenReturn("GESTION_UTILISATEURS");
        @SuppressWarnings("rawtypes")
        Collection<? extends GrantedAuthority> adminAuthorities = (Collection) List.of(adminAuthority);
        doReturn(adminAuthorities).when(authenticationAdmin).getAuthorities();

        // Mock Authentication sans droits admin
        authenticationUser = mock(Authentication.class);
        GrantedAuthority userAuthority = mock(GrantedAuthority.class);
        when(userAuthority.getAuthority()).thenReturn("BASIC_USER");
        @SuppressWarnings("rawtypes")
        Collection<? extends GrantedAuthority> userAuthorities = (Collection) List.of(userAuthority);
        doReturn(userAuthorities).when(authenticationUser).getAuthorities();
        when(authenticationUser.getPrincipal()).thenReturn(utilisateur);
    }

    // ========== Tests pour consulterLaListeDesUtilisateurs() ==========

    @Test
    @DisplayName("consulterLaListeDesUtilisateurs - Devrait retourner 200 OK avec la liste des utilisateurs")
    void consulterLaListeDesUtilisateurs_ShouldReturn200WithUserList() throws Exception {
        // Given
        List<ProfilDto> listeUtilisateurs = List.of(profilDto);
        when(utilisateurService.getAllUtilisateursDTO()).thenReturn(listeUtilisateurs);

        // When & Then
        mockMvc.perform(get("/api/v1/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].tokenId").value("token-123"))
                .andExpect(jsonPath("$[0].nom").value("Dupont"))
                .andExpect(jsonPath("$[0].prenom").value("Jean"));

        verify(utilisateurService).getAllUtilisateursDTO();
    }

    @Test
    @DisplayName("consulterLaListeDesUtilisateurs - Devrait retourner 200 OK avec une liste vide")
    void consulterLaListeDesUtilisateurs_WithEmptyList_ShouldReturn200WithEmptyList() throws Exception {
        // Given
        when(utilisateurService.getAllUtilisateursDTO()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/v1/utilisateurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());

        verify(utilisateurService).getAllUtilisateursDTO();
    }

    // ========== Tests pour consulterLaListeDesUtilisateursParRole() ==========

    @Test
    @DisplayName("consulterLaListeDesUtilisateursParRole - Devrait retourner 200 OK avec la liste filtrée par rôle")
    void consulterLaListeDesUtilisateursParRole_ShouldReturn200WithFilteredList() throws Exception {
        // Given
        List<ProfilDto> listeUtilisateurs = List.of(profilDto);
        when(utilisateurService.getUtilisateursByRole(Role.BASIC_USER)).thenReturn(listeUtilisateurs);

        // When & Then
        mockMvc.perform(get("/api/v1/utilisateurs/BASIC_USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].role").value("BASIC_USER"));

        verify(utilisateurService).getUtilisateursByRole(Role.BASIC_USER);
    }

    // ========== Tests pour chercherUtilisateurParEmail() ==========

    @Test
    @DisplayName("chercherUtilisateurParEmail - Devrait retourner 200 OK avec le profil utilisateur")
    void chercherUtilisateurParEmail_WithValidEmail_ShouldReturn200WithProfile() throws Exception {
        // Given
        when(utilisateurService.getUtilisateurByEmail("jean.dupont@test.fr"))
                .thenReturn(Optional.of(utilisateur));
        when(utilisateurService.mapUtilisateurToProfilDTO(utilisateur)).thenReturn(profilDto);

        // When & Then
        mockMvc.perform(get("/api/v1/utilisateurs/search")
                        .param("email", "jean.dupont@test.fr"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenId").value("token-123"))
                .andExpect(jsonPath("$.email").value("jean.dupont@test.fr"));

        verify(utilisateurService).getUtilisateurByEmail("jean.dupont@test.fr");
        verify(utilisateurService).mapUtilisateurToProfilDTO(utilisateur);
    }

    @Test
    @DisplayName("chercherUtilisateurParEmail - Devrait retourner 400 Bad Request si l'utilisateur est DIRECTION")
    void chercherUtilisateurParEmail_WithDirectionRole_ShouldReturn400BadRequest() throws Exception {
        // Given
        Utilisateur directionUser = Utilisateur.builder()
                .id(2)
                .tokenId("token-direction")
                .role(Role.DIRECTION)
                .nom("Admin")
                .prenom("Direction")
                .email("direction@test.fr")
                .build();

        when(utilisateurService.getUtilisateurByEmail("direction@test.fr"))
                .thenReturn(Optional.of(directionUser));

        // When & Then
        mockMvc.perform(get("/api/v1/utilisateurs/search")
                        .param("email", "direction@test.fr"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vous ne pouvez pas ajouter cette personne"))
                .andExpect(jsonPath("$.path").value("/api/v1/utilisateurs/search"));

        verify(utilisateurService).getUtilisateurByEmail("direction@test.fr");
        verify(utilisateurService, never()).mapUtilisateurToProfilDTO(any());
    }

    @Test
    @DisplayName("chercherUtilisateurParEmail - Devrait retourner 400 Bad Request si l'utilisateur est ADMIN")
    void chercherUtilisateurParEmail_WithAdminRole_ShouldReturn400BadRequest() throws Exception {
        // Given
        Utilisateur adminUser = Utilisateur.builder()
                .id(3)
                .tokenId("token-admin")
                .role(Role.ADMIN)
                .nom("Admin")
                .prenom("Super")
                .email("admin@test.fr")
                .build();

        when(utilisateurService.getUtilisateurByEmail("admin@test.fr"))
                .thenReturn(Optional.of(adminUser));

        // When & Then
        mockMvc.perform(get("/api/v1/utilisateurs/search")
                        .param("email", "admin@test.fr"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Vous ne pouvez pas ajouter cette personne"));

        verify(utilisateurService).getUtilisateurByEmail("admin@test.fr");
        verify(utilisateurService, never()).mapUtilisateurToProfilDTO(any());
    }

    @Test
    @DisplayName("chercherUtilisateurParEmail - Devrait retourner 404 Not Found si l'utilisateur n'existe pas")
    void chercherUtilisateurParEmail_WithNonExistentEmail_ShouldReturn404NotFound() throws Exception {
        // Given
        when(utilisateurService.getUtilisateurByEmail("inexistant@test.fr"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/utilisateurs/search")
                        .param("email", "inexistant@test.fr"))
                .andExpect(status().isNotFound());

        verify(utilisateurService).getUtilisateurByEmail("inexistant@test.fr");
        verify(utilisateurService, never()).mapUtilisateurToProfilDTO(any());
    }

    // ========== Tests pour profilUtilisateur() ==========

    @Test
    @DisplayName("profilUtilisateur - Devrait retourner 200 OK avec le profil utilisateur")
    void profilUtilisateur_WithValidTokenId_ShouldReturn200WithProfile() throws Exception {
        // Given
        when(utilisateurService.profilUtilisateur("token-123"))
                .thenReturn(Optional.of(utilisateur));
        when(utilisateurService.mapUtilisateurToProfilDTO(utilisateur)).thenReturn(profilDto);

        // When & Then
        mockMvc.perform(get("/api/v1/utilisateurs/profil")
                        .param("tokenId", "token-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenId").value("token-123"))
                .andExpect(jsonPath("$.nom").value("Dupont"));

        verify(utilisateurService).profilUtilisateur("token-123");
        verify(utilisateurService).mapUtilisateurToProfilDTO(utilisateur);
    }

    @Test
    @DisplayName("profilUtilisateur - Devrait retourner 404 Not Found si l'utilisateur n'existe pas")
    void profilUtilisateur_WithNonExistentTokenId_ShouldReturn404NotFound() throws Exception {
        // Given
        when(utilisateurService.profilUtilisateur("token-inexistant"))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/utilisateurs/profil")
                        .param("tokenId", "token-inexistant"))
                .andExpect(status().isNotFound());

        verify(utilisateurService).profilUtilisateur("token-inexistant");
        verify(utilisateurService, never()).mapUtilisateurToProfilDTO(any());
    }

    // ========== Tests pour supprimerUtilisateur() ==========

    @Test
    @DisplayName("supprimerUtilisateur - Devrait retourner 204 No Content")
    void supprimerUtilisateur_WithValidTokenId_ShouldReturn204NoContent() throws Exception {
        // Given
        doNothing().when(utilisateurService).supprimerUtilisateur("token-123");

        // When & Then
        mockMvc.perform(delete("/api/v1/utilisateurs/token-123"))
                .andExpect(status().isNoContent());

        verify(utilisateurService).supprimerUtilisateur("token-123");
    }

    // ========== Tests pour modifierUtilisateur() ==========

    @Test
    @DisplayName("modifierUtilisateur - Devrait retourner 200 OK avec le profil modifié par admin")
    @SuppressWarnings("null")
    void modifierUtilisateur_ByAdmin_ShouldReturn200WithUpdatedProfile() throws Exception {
        // Given
        Utilisateur updatedUser = Utilisateur.builder()
                .id(1)
                .tokenId("token-123")
                .role(Role.BASIC_USER)
                .nom("Martin")
                .prenom("Pierre")
                .genre(Genre.Masculin)
                .email("pierre.martin@test.fr")
                .telephone("0987654321")
                .dateNaissance(updateUserRequest.dateNaissance())
                .build();

        ProfilDto updatedProfilDto = new ProfilDto(
                "token-123",
                Role.BASIC_USER,
                null,
                "Martin",
                "Pierre",
                Genre.Masculin,
                "pierre.martin@test.fr",
                "0987654321",
                updateUserRequest.dateNaissance(),
                updateUserRequest.dateExpirationCompte()
        );

        when(utilisateurService.profilUtilisateur("token-123"))
                .thenReturn(Optional.of(utilisateur));
        when(utilisateurService.modifUserByAdmin(utilisateur, updateUserRequest))
                .thenReturn(updatedUser);
        when(utilisateurService.mapUtilisateurToProfilDTO(updatedUser))
                .thenReturn(updatedProfilDto);

        // When & Then
        mockMvc.perform(put("/api/v1/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest))
                        .principal(authenticationAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Martin"))
                .andExpect(jsonPath("$.prenom").value("Pierre"));

        verify(utilisateurService).profilUtilisateur("token-123");
        verify(utilisateurService).modifUserByAdmin(utilisateur, updateUserRequest);
        verify(utilisateurService, never()).modifUserByUser(any(), any());
        verify(utilisateurService).mapUtilisateurToProfilDTO(updatedUser);
    }

    @Test
    @DisplayName("modifierUtilisateur - Devrait retourner 200 OK avec le profil modifié par utilisateur")
    @SuppressWarnings("null")
    void modifierUtilisateur_ByUser_ShouldReturn200WithUpdatedProfile() throws Exception {
        // Given
        Utilisateur updatedUser = Utilisateur.builder()
                .id(1)
                .tokenId("token-123")
                .role(Role.BASIC_USER)
                .nom("Martin")
                .prenom("Pierre")
                .genre(Genre.Masculin)
                .email("pierre.martin@test.fr")
                .telephone("0987654321")
                .dateNaissance(updateUserRequest.dateNaissance())
                .build();

        ProfilDto updatedProfilDto = new ProfilDto(
                "token-123",
                Role.BASIC_USER,
                null,
                "Martin",
                "Pierre",
                Genre.Masculin,
                "pierre.martin@test.fr",
                "0987654321",
                updateUserRequest.dateNaissance(),
                updateUserRequest.dateExpirationCompte()
        );

        when(utilisateurService.profilUtilisateur("token-123"))
                .thenReturn(Optional.of(utilisateur));
        when(utilisateurService.modifUserByUser(utilisateur, updateUserRequest))
                .thenReturn(updatedUser);
        when(utilisateurService.mapUtilisateurToProfilDTO(updatedUser))
                .thenReturn(updatedProfilDto);

        // When & Then
        mockMvc.perform(put("/api/v1/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest))
                        .principal(authenticationUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nom").value("Martin"))
                .andExpect(jsonPath("$.prenom").value("Pierre"));

        verify(utilisateurService).profilUtilisateur("token-123");
        verify(utilisateurService).modifUserByUser(utilisateur, updateUserRequest);
        verify(utilisateurService, never()).modifUserByAdmin(any(), any());
        verify(utilisateurService).mapUtilisateurToProfilDTO(updatedUser);
    }

    @Test
    @DisplayName("modifierUtilisateur - Devrait retourner 404 Not Found si l'utilisateur n'existe pas")
    @SuppressWarnings("null")
    void modifierUtilisateur_WithNonExistentTokenId_ShouldReturn404NotFound() throws Exception {
        // Given
        when(utilisateurService.profilUtilisateur("token-inexistant"))
                .thenReturn(Optional.empty());

        UpdateUserRequest request = new UpdateUserRequest(
                "token-inexistant",
                "Jean",
                "Dupont",
                "M",
                "jean.dupont@test.fr",
                "0123456789",
                new Date(),
                Role.BASIC_USER,
                Instant.now()
        );

        // When & Then
        mockMvc.perform(put("/api/v1/utilisateurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .principal(authenticationAdmin))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Utilisateur non trouvé avec le token ID: token-inexistant"));

        verify(utilisateurService).profilUtilisateur("token-inexistant");
        verify(utilisateurService, never()).modifUserByAdmin(any(), any());
        verify(utilisateurService, never()).modifUserByUser(any(), any());
    }

    // ========== Tests pour changerMotDePasse() ==========

    @Test
    @DisplayName("changerMotDePasse - Devrait retourner 200 OK quand l'admin change le mot de passe")
    @SuppressWarnings("null")
    void changerMotDePasse_ByAdmin_ShouldReturn200Ok() throws Exception {
        // Given
        ChangePasswordRequest adminRequest = new ChangePasswordRequest(
                "token-123",
                null,
                "NewPassword123!"
        );

        when(utilisateurService.changerMotDePasseParAdmin("token-123", "NewPassword123!"))
                .thenReturn(utilisateur);

        // When & Then
        mockMvc.perform(patch("/api/v1/utilisateurs/mot-de-passe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminRequest))
                        .principal(authenticationAdmin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Mot de passe modifié avec succès"));

        verify(utilisateurService).changerMotDePasseParAdmin("token-123", "NewPassword123!");
        verify(utilisateurService, never()).changerMotDePasseParUtilisateur(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("changerMotDePasse - Devrait retourner 200 OK quand l'utilisateur change son propre mot de passe")
    @SuppressWarnings("null")
    void changerMotDePasse_ByUser_WithOwnTokenId_ShouldReturn200Ok() throws Exception {
        // Given
        when(utilisateurService.changerMotDePasseParUtilisateur(
                "token-123", "OldPassword123!", "NewPassword123!"))
                .thenReturn(utilisateur);

        // When & Then
        mockMvc.perform(patch("/api/v1/utilisateurs/mot-de-passe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordRequest))
                        .principal(authenticationUser))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Mot de passe modifié avec succès"));

        verify(utilisateurService).changerMotDePasseParUtilisateur(
                "token-123", "OldPassword123!", "NewPassword123!");
        verify(utilisateurService, never()).changerMotDePasseParAdmin(anyString(), anyString());
    }

    @Test
    @DisplayName("changerMotDePasse - Devrait retourner 403 Forbidden quand l'utilisateur essaie de changer le mot de passe d'un autre")
    @SuppressWarnings("null")
    void changerMotDePasse_ByUser_WithDifferentTokenId_ShouldReturn403Forbidden() throws Exception {
        // Given
        ChangePasswordRequest otherUserRequest = new ChangePasswordRequest(
                "token-other",
                "OldPassword123!",
                "NewPassword123!"
        );

        // When & Then
        mockMvc.perform(patch("/api/v1/utilisateurs/mot-de-passe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(otherUserRequest))
                        .principal(authenticationUser))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Vous ne pouvez modifier que votre propre mot de passe"))
                .andExpect(jsonPath("$.path").value("/api/v1/utilisateurs/mot-de-passe"));

        verify(utilisateurService, never()).changerMotDePasseParUtilisateur(anyString(), anyString(), anyString());
        verify(utilisateurService, never()).changerMotDePasseParAdmin(anyString(), anyString());
    }

    @Test
    @DisplayName("changerMotDePasse - Devrait retourner 400 Bad Request si l'ancien mot de passe est manquant pour un utilisateur")
    @SuppressWarnings("null")
    void changerMotDePasse_ByUser_WithoutOldPassword_ShouldReturn400BadRequest() throws Exception {
        // Given
        ChangePasswordRequest requestWithoutOldPassword = new ChangePasswordRequest(
                "token-123",
                null,
                "NewPassword123!"
        );

        // When & Then
        mockMvc.perform(patch("/api/v1/utilisateurs/mot-de-passe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithoutOldPassword))
                        .principal(authenticationUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("L'ancien mot de passe est obligatoire"))
                .andExpect(jsonPath("$.path").value("/api/v1/utilisateurs/mot-de-passe"));

        verify(utilisateurService, never()).changerMotDePasseParUtilisateur(anyString(), anyString(), anyString());
        verify(utilisateurService, never()).changerMotDePasseParAdmin(anyString(), anyString());
    }

    @Test
    @DisplayName("changerMotDePasse - Devrait retourner 400 Bad Request si l'ancien mot de passe est vide pour un utilisateur")
    @SuppressWarnings("null")
    void changerMotDePasse_ByUser_WithBlankOldPassword_ShouldReturn400BadRequest() throws Exception {
        // Given
        ChangePasswordRequest requestWithBlankOldPassword = new ChangePasswordRequest(
                "token-123",
                "   ",
                "NewPassword123!"
        );

        // When & Then
        mockMvc.perform(patch("/api/v1/utilisateurs/mot-de-passe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestWithBlankOldPassword))
                        .principal(authenticationUser))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("L'ancien mot de passe est obligatoire"));

        verify(utilisateurService, never()).changerMotDePasseParUtilisateur(anyString(), anyString(), anyString());
        verify(utilisateurService, never()).changerMotDePasseParAdmin(anyString(), anyString());
    }
}
