package com.tarnof.enjoyrestapi.services.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.exceptions.EmailDejaUtiliseException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.AuthenticationRequest;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import com.tarnof.enjoyrestapi.payload.response.AuthenticationResponse;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.JwtService;
import com.tarnof.enjoyrestapi.services.RefreshTokenService;
import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.entities.Utilisateur;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour AuthenticationServiceImpl")
public class AuthenticationServiceImplTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UtilisateurRepository utilisateurRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private RefreshTokenService refreshTokenService;
    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private Date dateNaissance;
    private Instant dateExpiration;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private RefreshToken refreshToken;
    private Utilisateur utilisateur;
    
    @BeforeEach
    void setUp() {
        dateNaissance = new Date(System.currentTimeMillis() - 86400000L * 365 * 25);
        dateExpiration = Instant.now().plusSeconds(86400);

        registerRequest = new RegisterRequest(
            "John",
            "Doe",
            "Homme",
            dateNaissance,
            "0123456789",
            "john.doe@example.com",
            "password",
            dateExpiration,
            Role.BASIC_USER,
           null
        );

        authenticationRequest = new AuthenticationRequest(
            "john.doe@example.com",
            "password"
        );

        refreshToken = RefreshToken.builder()
            .id(1L)
            .token("refresh-token-123")
            .expiryDate(dateExpiration)
            .build();

        utilisateur = Utilisateur.builder()
            .id(1)
            .tokenId("user-token-123")
            .nom("John")
            .prenom("Doe")
            .role(Role.BASIC_USER)
            .email("john.doe@example.com")
            .motDePasse("password")
            .genre("Homme")
            .dateNaissance(dateNaissance)
            .telephone("0123456789")
            .refreshToken(refreshToken)
            .build();
    }

    @Test
    @DisplayName("register - Devrait créer un utilisateur avec succès")
    @SuppressWarnings("null")
    void register_WithValidData_ShouldCreateUser() {
        // Given 
        when(utilisateurRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.motDePasse())).thenReturn("encoded-password");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> {
            Utilisateur u = invocation.getArgument(0);
            u.setId(1); 
            return u;
        });
        when(jwtService.generateToken(any(Utilisateur.class))).thenReturn("jwt-token-123");
        when(refreshTokenService.createRefreshToken(anyInt(), any(Instant.class))).thenReturn(refreshToken);      
        // When
        AuthenticationResponse result = authenticationService.register(registerRequest);     
        // Then 
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("jwt-token-123");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-123");
        assertThat(result.role()).isEqualTo(Role.BASIC_USER);
        assertThat(result.tokenId()).isNotNull(); 
        assertThat(result.tokenId()).isNotEmpty();       
        verify(utilisateurRepository).existsByEmail(registerRequest.email());
        verify(passwordEncoder).encode(registerRequest.motDePasse());
        verify(utilisateurRepository).save(any(Utilisateur.class));
        verify(jwtService).generateToken(any(Utilisateur.class));
        verify(refreshTokenService).createRefreshToken(anyInt(), eq(dateExpiration));
    }

    @Test
    @DisplayName("register - Devrait lancer une exception si l'email est déjà utilisé")
    @SuppressWarnings("null")
    void register_WhenEmailAlreadyExists_ShouldThrowException() {
    // Given
    when(utilisateurRepository.existsByEmail(registerRequest.email())).thenReturn(true);
    // When & Then
    assertThatThrownBy(() -> authenticationService.register(registerRequest))
        .isInstanceOf(EmailDejaUtiliseException.class)
        .hasMessageContaining("Un compte avec cette adresse e-mail existe déjà.");
    verify(utilisateurRepository).existsByEmail(registerRequest.email());
    verify(utilisateurRepository, never()).save(any(Utilisateur.class));
    verify(passwordEncoder, never()).encode(anyString());
    verify(jwtService, never()).generateToken(any(Utilisateur.class));
    verify(refreshTokenService, never()).createRefreshToken(anyInt(), any(Instant.class));
    }

    @Test
    @DisplayName("register - Devrait lancer une exception si la date d'expiration est null")
    @SuppressWarnings("null")
    void register_WhenDateExpirationIsNull_ShouldThrowException() {
        // Given
        RegisterRequest requestWithoutDate = new RegisterRequest(
            registerRequest.prenom(),
            registerRequest.nom(),
            registerRequest.genre(),
            registerRequest.dateNaissance(),
            registerRequest.telephone(),
            registerRequest.email(),
            registerRequest.motDePasse(),
            null,  
            registerRequest.role(),
            registerRequest.roleSejour()
        );
        when(utilisateurRepository.existsByEmail(requestWithoutDate.email())).thenReturn(false);
        // When & Then
        assertThatThrownBy(() -> authenticationService.register(requestWithoutDate))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La date d'expiration est obligatoire pour l'inscription.");      
        verify(utilisateurRepository).existsByEmail(requestWithoutDate.email());
        verify(utilisateurRepository, never()).save(any(Utilisateur.class));
        verify(passwordEncoder, never()).encode(anyString());
        verify(jwtService, never()).generateToken(any(Utilisateur.class));
        verify(refreshTokenService, never()).createRefreshToken(anyInt(), any(Instant.class));
    }

    @Test
    @DisplayName("authenticate - Devrait authentifier un utilisateur avec succès")
    void authenticate_WithValidData_ShouldAuthenticateUser() {
        // Given
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            authenticationRequest.email(), 
            authenticationRequest.motDePasse()
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authToken);
        when(utilisateurRepository.findByEmail(authenticationRequest.email())).thenReturn(Optional.of(utilisateur));
        when(jwtService.generateToken(utilisateur)).thenReturn("jwt-token-123");
        when(refreshTokenService.findByUtilisateur(utilisateur)).thenReturn(Optional.of(refreshToken));      
        // When
        AuthenticationResponse result = authenticationService.authenticate(authenticationRequest);      
        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("jwt-token-123");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-123");
        assertThat(result.role()).isEqualTo(Role.BASIC_USER);
        assertThat(result.tokenId()).isEqualTo("user-token-123");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(utilisateurRepository).findByEmail(authenticationRequest.email());
        verify(jwtService).generateToken(utilisateur);
        verify(refreshTokenService).findByUtilisateur(utilisateur);
    }

    @Test
    @DisplayName("authenticate - Devrait lancer une exception si les identifiants sont invalides")
    void authenticate_WhenInvalidCredentials_ShouldThrowException() {
    // Given
    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
    .thenThrow(new BadCredentialsException("Identifiants invalides"));
    // When & Then
    assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
    .isInstanceOf(BadCredentialsException.class)
    .hasMessageContaining("Identifiants invalides");
    verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    verify(utilisateurRepository, never()).findByEmail(anyString());
    verify(jwtService, never()).generateToken(any(Utilisateur.class));
    verify(refreshTokenService, never()).findByUtilisateur(any(Utilisateur.class));
    }

    @Test
    @DisplayName("authenticate - Devrait lancer une exception si l'utilisateur n'existe pas")
    void authenticate_WhenUserNotFound_ShouldThrowException() {
        // Given
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            authenticationRequest.email(), 
            authenticationRequest.motDePasse()
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authToken);
        when(utilisateurRepository.findByEmail(authenticationRequest.email()))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Utilisateur introuvable");       
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(utilisateurRepository).findByEmail(authenticationRequest.email());
        verify(jwtService, never()).generateToken(any(Utilisateur.class));
        verify(refreshTokenService, never()).findByUtilisateur(any(Utilisateur.class));
    }

    @Test
    @DisplayName("authenticate - Devrait lancer une exception si le refresh token n'existe pas")
    void authenticate_WhenRefreshTokenNotFound_ShouldThrowException() {
        // Given
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
            authenticationRequest.email(), 
            authenticationRequest.motDePasse()
        );
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authToken);
        when(utilisateurRepository.findByEmail(authenticationRequest.email())).thenReturn(Optional.of(utilisateur));
        when(jwtService.generateToken(utilisateur)).thenReturn("jwt-token-123");
        when(refreshTokenService.findByUtilisateur(utilisateur)).thenReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Refresh token introuvable");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(utilisateurRepository).findByEmail(authenticationRequest.email());
        verify(jwtService).generateToken(utilisateur);
        verify(refreshTokenService).findByUtilisateur(utilisateur);
    }
}
