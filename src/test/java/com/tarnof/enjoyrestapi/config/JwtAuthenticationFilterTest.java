package com.tarnof.enjoyrestapi.config;

import com.tarnof.enjoyrestapi.services.impl.JwtServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour JwtAuthenticationFilter")
class JwtAuthenticationFilterTest {

    @Mock
    private JwtServiceImpl jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private UserDetails userDetails;
    private String validToken;
    private String userEmail;

    @BeforeEach
    void setUp() {
        // Nettoyer le SecurityContext avant chaque test
        SecurityContextHolder.clearContext();

        userEmail = "user@test.fr";
        validToken = "valid.jwt.token";

        userDetails = User.builder()
                .username(userEmail)
                .password("password")
                .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @Test
    @DisplayName("doFilterInternal - Devrait continuer la chaîne si pas de header Authorization")
    @SuppressWarnings("null")
    void doFilterInternal_WhenNoAuthorizationHeader_ShouldContinueChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUserName(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - Devrait continuer la chaîne si header Authorization ne commence pas par Bearer")
    @SuppressWarnings("null")
    void doFilterInternal_WhenAuthorizationHeaderDoesNotStartWithBearer_ShouldContinueChain() throws ServletException, IOException {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Invalid token");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUserName(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - Devrait authentifier avec succès avec un token valide")
    @SuppressWarnings("null")
    void doFilterInternal_WithValidToken_ShouldAuthenticate() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUserName(validToken);
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(jwtService).isTokenValid(validToken, userDetails);
        verify(filterChain).doFilter(request, response);

        // Vérifier que l'authentification est définie dans le SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getPrincipal()).isEqualTo(userDetails);
        assertThat(authentication.getAuthorities()).isNotEmpty();
        assertThat(authentication.getAuthorities().size()).isEqualTo(userDetails.getAuthorities().size());
    }

    @Test
    @DisplayName("doFilterInternal - Devrait continuer la chaîne si le token est invalide")
    @SuppressWarnings("null")
    void doFilterInternal_WithInvalidToken_ShouldContinueChain() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer invalid.token";
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName("invalid.token")).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid("invalid.token", userDetails)).thenReturn(false);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUserName("invalid.token");
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(jwtService).isTokenValid("invalid.token", userDetails);
        verify(filterChain).doFilter(request, response);

        // L'authentification ne doit pas être définie
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - Devrait continuer la chaîne si le token est expiré")
    @SuppressWarnings("null")
    void doFilterInternal_WithExpiredToken_ShouldContinueChain() throws ServletException, IOException {
        // Given
        String expiredToken = "expired.token";
        String authHeader = "Bearer " + expiredToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(expiredToken)).thenThrow(new ExpiredJwtException(null, null, "Token expired"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUserName(expiredToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).isTokenValid(anyString(), any(UserDetails.class));
        verify(filterChain).doFilter(request, response);

        // L'authentification ne doit pas être définie
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - Devrait continuer la chaîne si JwtException est lancée")
    @SuppressWarnings("null")
    void doFilterInternal_WithJwtException_ShouldContinueChain() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid.token";
        String authHeader = "Bearer " + invalidToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(invalidToken)).thenThrow(new JwtException("Invalid token"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUserName(invalidToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).isTokenValid(anyString(), any(UserDetails.class));
        verify(filterChain).doFilter(request, response);

        // L'authentification ne doit pas être définie
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - Devrait continuer la chaîne si une exception générique est lancée")
    @SuppressWarnings("null")
    void doFilterInternal_WithGenericException_ShouldContinueChain() throws ServletException, IOException {
        // Given
        String token = "token";
        String authHeader = "Bearer " + token;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(token)).thenThrow(new RuntimeException("Unexpected error"));

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUserName(token);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).isTokenValid(anyString(), any(UserDetails.class));
        verify(filterChain).doFilter(request, response);

        // L'authentification ne doit pas être définie
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - Ne devrait pas authentifier si l'email extrait est vide")
    @SuppressWarnings("null")
    void doFilterInternal_WhenExtractedEmailIsEmpty_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(validToken)).thenReturn("");

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUserName(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).isTokenValid(anyString(), any(UserDetails.class));
        verify(filterChain).doFilter(request, response);

        // L'authentification ne doit pas être définie
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilterInternal - Ne devrait pas authentifier si une authentification existe déjà")
    @SuppressWarnings("null")
    void doFilterInternal_WhenAuthenticationAlreadyExists_ShouldNotAuthenticate() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(validToken)).thenReturn(userEmail);

        // Définir une authentification existante dans le SecurityContext
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication existingAuth = mock(Authentication.class);
        context.setAuthentication(existingAuth);
        SecurityContextHolder.setContext(context);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUserName(validToken);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtService, never()).isTokenValid(anyString(), any(UserDetails.class));
        verify(filterChain).doFilter(request, response);

        // L'authentification existante doit être préservée
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
    }

    @Test
    @DisplayName("doFilterInternal - Devrait extraire correctement le token depuis le header Bearer")
    @SuppressWarnings("null")
    void doFilterInternal_ShouldExtractTokenFromBearerHeader() throws ServletException, IOException {
        // Given
        String token = "my.jwt.token";
        String authHeader = "Bearer " + token;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(token)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        // Vérifier que extractUserName est appelé avec le token extrait (sans "Bearer ")
        verify(jwtService).extractUserName(token);
        verify(userDetailsService).loadUserByUsername(userEmail);
        verify(jwtService).isTokenValid(token, userDetails);
    }

    @Test
    @DisplayName("doFilterInternal - Devrait définir les détails de l'authentification avec WebAuthenticationDetailsSource")
    @SuppressWarnings("null")
    void doFilterInternal_ShouldSetAuthenticationDetails() throws ServletException, IOException {
        // Given
        String authHeader = "Bearer " + validToken;
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtService.extractUserName(validToken)).thenReturn(userEmail);
        when(userDetailsService.loadUserByUsername(userEmail)).thenReturn(userDetails);
        when(jwtService.isTokenValid(validToken, userDetails)).thenReturn(true);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getDetails()).isNotNull();
    }
}
