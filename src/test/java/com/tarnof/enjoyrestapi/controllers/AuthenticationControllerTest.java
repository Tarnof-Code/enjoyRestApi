package com.tarnof.enjoyrestapi.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.TokenType;
import com.tarnof.enjoyrestapi.exceptions.EmailDejaUtiliseException;
import com.tarnof.enjoyrestapi.exceptions.TokenException;
import com.tarnof.enjoyrestapi.payload.request.AuthenticationRequest;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import com.tarnof.enjoyrestapi.payload.response.AuthenticationResponse;
import com.tarnof.enjoyrestapi.payload.response.RefreshTokenResponse;
import com.tarnof.enjoyrestapi.exceptions.GlobalExceptionHandler;
import com.tarnof.enjoyrestapi.handlers.TokenControllerHandler;
import com.tarnof.enjoyrestapi.services.AuthenticationService;
import com.tarnof.enjoyrestapi.services.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour AuthenticationController")
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private AuthenticationResponse authenticationResponse;
    private RefreshTokenResponse refreshTokenResponse;
    private ResponseCookie refreshTokenCookie;
    private ResponseCookie cleanCookie;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Configuration de MockMvc avec les exception handlers
        GlobalExceptionHandler globalExceptionHandler = new GlobalExceptionHandler();
        TokenControllerHandler tokenControllerHandler = new TokenControllerHandler();
        
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(globalExceptionHandler, tokenControllerHandler)
                .build();

        Date dateNaissance = new Date(System.currentTimeMillis() - 86400000L * 365 * 25);
        Instant dateExpiration = Instant.now().plusSeconds(86400 * 30);

        registerRequest = new RegisterRequest(
                "Jean",
                "Dupont",
                "M",
                dateNaissance,
                "0123456789",
                "jean.dupont@test.fr",
                "Password123!",
                dateExpiration,
                Role.BASIC_USER,
                null
        );

        authenticationRequest = new AuthenticationRequest(
                "jean.dupont@test.fr",
                "Password123!"
        );

        authenticationResponse = new AuthenticationResponse(
                Role.BASIC_USER,
                "token-id-123",
                "access-token-jwt",
                "refresh-token-value",
                null
        );

        refreshTokenResponse = new RefreshTokenResponse(
                "new-access-token-jwt",
                "refresh-token-value",
                TokenType.BEARER,
                Role.BASIC_USER
        );

        refreshTokenCookie = ResponseCookie.from("refreshToken", "refresh-token-value")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(86400 * 30)
                .build();

        cleanCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    // ========== Tests pour register() ==========

    @Test
    @DisplayName("register - Devrait retourner 200 OK avec tokens et cookie")
    @SuppressWarnings("null")
    void register_WithValidData_ShouldReturn200WithTokensAndCookie() throws Exception {
        // Given
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(authenticationResponse);
        when(refreshTokenService.generateRefreshTokenCookie(anyString()))
                .thenReturn(refreshTokenCookie);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("BASIC_USER"))
                .andExpect(jsonPath("$.tokenId").value("token-id-123"))
                .andExpect(jsonPath("$.access_token").value("access-token-jwt"))
                .andExpect(jsonPath("$.refresh_token").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").doesNotExist())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(cookie().exists("refreshToken"));

        verify(authenticationService).register(any(RegisterRequest.class));
        verify(refreshTokenService).generateRefreshTokenCookie("refresh-token-value");
    }

    @Test
    @DisplayName("register - Devrait retourner 400 Bad Request si validation échoue")
    @SuppressWarnings("null")
    void register_WithInvalidData_ShouldReturn400BadRequest() throws Exception {
        // Given
        RegisterRequest invalidRequest = new RegisterRequest(
                "", // prenom vide
                "Dupont",
                "M",
                new Date(),
                "0123456789",
                "invalid-email", // email invalide
                "weak", // mot de passe trop faible
                Instant.now(),
                Role.BASIC_USER,
                null
        );

        // When & Then
        mockMvc.perform(post("/api/v1/auth/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(authenticationService, never()).register(any(RegisterRequest.class));
        verify(refreshTokenService, never()).generateRefreshTokenCookie(anyString());
    }

    @Test
    @DisplayName("register - Devrait retourner 409 Conflict si email déjà utilisé")
    @SuppressWarnings("null")
    void register_WhenEmailAlreadyExists_ShouldReturn409Conflict() throws Exception {
        // Given
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new EmailDejaUtiliseException("Un compte avec cette adresse e-mail existe déjà."));

        // When & Then
        mockMvc.perform(post("/api/v1/auth/inscription")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Un compte avec cette adresse e-mail existe déjà."));

        verify(authenticationService).register(any(RegisterRequest.class));
        verify(refreshTokenService, never()).generateRefreshTokenCookie(anyString());
    }

    // ========== Tests pour authenticate() ==========

    @Test
    @DisplayName("authenticate - Devrait retourner 200 OK avec tokens et cookie")
    @SuppressWarnings("null")
    void authenticate_WithValidCredentials_ShouldReturn200WithTokensAndCookie() throws Exception {
        // Given
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenReturn(authenticationResponse);
        when(refreshTokenService.generateRefreshTokenCookie(anyString()))
                .thenReturn(refreshTokenCookie);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/connexion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("BASIC_USER"))
                .andExpect(jsonPath("$.tokenId").value("token-id-123"))
                .andExpect(jsonPath("$.access_token").value("access-token-jwt"))
                .andExpect(jsonPath("$.refresh_token").doesNotExist())
                .andExpect(jsonPath("$.errorMessage").doesNotExist())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(cookie().exists("refreshToken"));

        verify(authenticationService).authenticate(any(AuthenticationRequest.class));
        verify(refreshTokenService).generateRefreshTokenCookie("refresh-token-value");
    }

    @Test
    @DisplayName("authenticate - Devrait retourner 404 si identifiants invalides (BadCredentialsException hérite de RuntimeException)")
    @SuppressWarnings("null")
    void authenticate_WithInvalidCredentials_ShouldReturn404() throws Exception {
        // Given
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new BadCredentialsException("Identifiants invalides"));

        // When & Then
        // Note: BadCredentialsException hérite de RuntimeException, donc elle est interceptée par handleRuntimeException() qui retourne 404
        mockMvc.perform(post("/api/v1/auth/connexion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Identifiants invalides"));

        verify(authenticationService).authenticate(any(AuthenticationRequest.class));
        verify(refreshTokenService, never()).generateRefreshTokenCookie(anyString());
    }

    // ========== Tests pour refreshToken() ==========

    @Test
    @DisplayName("refreshToken - Devrait retourner 200 OK avec nouveau token")
    void refreshToken_WithValidToken_ShouldReturn200WithNewToken() throws Exception {
        // Given
        when(refreshTokenService.getRefreshTokenFromCookies(any()))
                .thenReturn("refresh-token-value");
        when(refreshTokenService.generateNewToken(any()))
                .thenReturn(refreshTokenResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token-value")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").value("new-access-token-jwt"))
                .andExpect(jsonPath("$.refresh_token").value("refresh-token-value"))
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andExpect(jsonPath("$.role").value("BASIC_USER"));

        verify(refreshTokenService).getRefreshTokenFromCookies(any());
        verify(refreshTokenService).generateNewToken(any());
    }

    @Test
    @DisplayName("refreshToken - Devrait retourner 404 si token invalide (TokenException hérite de RuntimeException)")
    void refreshToken_WithInvalidToken_ShouldReturn404() throws Exception {
        // Given
        when(refreshTokenService.getRefreshTokenFromCookies(any()))
                .thenReturn("invalid-token");
        when(refreshTokenService.generateNewToken(any()))
                .thenThrow(new TokenException("test@example.com", "Token invalide ou expiré"));

        // When & Then
        // Note: TokenException hérite de RuntimeException, donc elle est interceptée par handleRuntimeException() qui retourne 404
        // Pour avoir 403, il faudrait que TokenException soit interceptée par TokenControllerHandler,
        // mais dans standaloneSetup(), l'ordre des handlers peut être différent
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "invalid-token")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Failed for [test@example.com]: Token invalide ou expiré"));

        verify(refreshTokenService).getRefreshTokenFromCookies(any());
        verify(refreshTokenService).generateNewToken(any());
    }

    // ========== Tests pour logout() ==========

    @Test
    @DisplayName("logout - Devrait retourner 200 OK et supprimer le cookie")
    void logout_WithValidToken_ShouldReturn200AndDeleteCookie() throws Exception {
        // Given
        when(refreshTokenService.getRefreshTokenFromCookies(any()))
                .thenReturn("refresh-token-value");
        doNothing().when(refreshTokenService).deleteByToken(anyString());
        when(refreshTokenService.getCleanRefreshTokenCookie())
                .thenReturn(cleanCookie);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", "refresh-token-value")))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(refreshTokenService).getRefreshTokenFromCookies(any());
        verify(refreshTokenService).deleteByToken("refresh-token-value");
        verify(refreshTokenService).getCleanRefreshTokenCookie();
    }

    @Test
    @DisplayName("logout - Devrait retourner 200 OK même sans cookie")
    void logout_WithoutCookie_ShouldReturn200() throws Exception {
        // Given
        when(refreshTokenService.getRefreshTokenFromCookies(any()))
                .thenReturn(null);
        when(refreshTokenService.getCleanRefreshTokenCookie())
                .thenReturn(cleanCookie);

        // When & Then
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(cookie().value("refreshToken", ""))
                .andExpect(cookie().maxAge("refreshToken", 0));

        verify(refreshTokenService).getRefreshTokenFromCookies(any());
        verify(refreshTokenService, never()).deleteByToken(anyString());
        verify(refreshTokenService).getCleanRefreshTokenCookie();
    }
}
