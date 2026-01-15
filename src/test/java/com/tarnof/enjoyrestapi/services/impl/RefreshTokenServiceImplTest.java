package com.tarnof.enjoyrestapi.services.impl;

import com.tarnof.enjoyrestapi.entities.RefreshToken;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.TokenType;
import com.tarnof.enjoyrestapi.exceptions.TokenException;
import com.tarnof.enjoyrestapi.payload.request.RefreshTokenRequest;
import com.tarnof.enjoyrestapi.payload.response.RefreshTokenResponse;
import com.tarnof.enjoyrestapi.repositories.RefreshTokenRepository;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour RefreshTokenServiceImpl")
public class RefreshTokenServiceImplTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    
    @Mock
    private JwtService jwtService;
    
    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    private Utilisateur utilisateur;
    private RefreshToken refreshToken;
    private Date dateNaissance;
    private Instant dateExpiration;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        dateNaissance = new Date(System.currentTimeMillis() - 86400000L * 365 * 25);
        dateExpiration = Instant.now().plusSeconds(86400);

        utilisateur = Utilisateur.builder()
                .id(1)
                .tokenId("user-token-123")
                .nom("Dupont")
                .prenom("Jean")
                .role(Role.BASIC_USER)
                .email("jean.dupont@test.fr")
                .telephone("0123456789")
                .genre("Homme")
                .dateNaissance(dateNaissance)
                .motDePasse("MotDePasse123!")
                .build();

        refreshToken = RefreshToken.builder()
                .id(1L)
                .token("refresh-token-123")
                .utilisateur(utilisateur)
                .expiryDate(dateExpiration)
                .revoked(false)
                .build();

        // Lire les propriétés depuis application-test.yml
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-test.yml"));
        var properties = Objects.requireNonNull(yaml.getObject(),
            "Les propriétés de test n'ont pas pu être chargées depuis application-test.yml");

        // Injecter les valeurs avec ReflectionTestUtils
        String refreshExpiration = properties.getProperty("application.security.jwt.refresh-token.expiration");
        String refreshTokenName = properties.getProperty("application.security.jwt.refresh-token.cookie-name");
        String secureCookie = properties.getProperty("application.security.jwt.refresh-token.secure");
        String sameSite = properties.getProperty("application.security.jwt.refresh-token.same-site");

        assertThat(refreshExpiration).isNotNull();
        assertThat(refreshTokenName).isNotNull();
        assertThat(secureCookie).isNotNull();
        assertThat(sameSite).isNotNull();

        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", Long.parseLong(refreshExpiration));
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenName", refreshTokenName);
        ReflectionTestUtils.setField(refreshTokenService, "secureCookie", Boolean.parseBoolean(secureCookie));
        ReflectionTestUtils.setField(refreshTokenService, "sameSite", sameSite);
    }

    @Test
    @DisplayName("createRefreshToken - Devrait créer un refresh token avec succès")
    @SuppressWarnings("null")
    void createRefreshToken_WithValidData_ShouldCreateToken() {
        // Given
        when(utilisateurRepository.findById(1)).thenReturn(Optional.of(utilisateur));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken rt = invocation.getArgument(0);
            rt.setId(1L);
            return rt;
        });

        // When
        RefreshToken result = refreshTokenService.createRefreshToken(1, dateExpiration);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUtilisateur()).isEqualTo(utilisateur);
        assertThat(result.getExpiryDate()).isEqualTo(dateExpiration);
        assertThat(result.isRevoked()).isFalse();
        assertThat(result.getToken()).isNotNull();
        assertThat(result.getToken()).isNotEmpty();
        verify(utilisateurRepository).findById(1);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("createRefreshToken - Devrait lancer une exception si l'utilisateur n'existe pas")
    @SuppressWarnings("null")
    void createRefreshToken_WhenUserNotFound_ShouldThrowException() {
        // Given
        when(utilisateurRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.createRefreshToken(999, dateExpiration))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("User not found");
        verify(utilisateurRepository).findById(999);
        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("verifyExpiration - Devrait retourner le token si valide")
    @SuppressWarnings("null")
    void verifyExpiration_WithValidToken_ShouldReturnToken() {
        // Given
        RefreshToken validToken = RefreshToken.builder()
                .id(1L)
                .token("valid-token")
                .expiryDate(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        // When
        RefreshToken result = refreshTokenService.verifyExpiration(validToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(validToken);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("verifyExpiration - Devrait lancer une exception si le token est null")
    @SuppressWarnings("null")
    void verifyExpiration_WithNullToken_ShouldThrowException() {
        // When & Then
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(null))
            .isInstanceOf(TokenException.class)
            .hasMessageContaining("Token is null");
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("verifyExpiration - Devrait lancer une exception si le token est expiré")
    @SuppressWarnings("null")
    void verifyExpiration_WithExpiredToken_ShouldThrowException() {
        // Given
        RefreshToken expiredToken = RefreshToken.builder()
                .id(1L)
                .token("expired-token")
                .expiryDate(Instant.now().minusSeconds(3600))
                .revoked(false)
                .build();

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expiredToken))
            .isInstanceOf(TokenException.class)
            .hasMessageContaining("Refresh token was expired");
        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("findByToken - Devrait retourner le token s'il existe")
    void findByToken_WhenTokenExists_ShouldReturnToken() {
        // Given
        when(refreshTokenRepository.findByToken("refresh-token-123")).thenReturn(Optional.of(refreshToken));

        // When
        Optional<RefreshToken> result = refreshTokenService.findByToken("refresh-token-123");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(refreshToken);
        verify(refreshTokenRepository).findByToken("refresh-token-123");
    }

    @Test
    @DisplayName("findByToken - Devrait retourner empty si le token n'existe pas")
    void findByToken_WhenTokenNotExists_ShouldReturnEmpty() {
        // Given
        when(refreshTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        // When
        Optional<RefreshToken> result = refreshTokenService.findByToken("non-existent-token");

        // Then
        assertThat(result).isEmpty();
        verify(refreshTokenRepository).findByToken("non-existent-token");
    }

    @Test
    @DisplayName("findByUtilisateur - Devrait retourner le token s'il existe")
    void findByUtilisateur_WhenTokenExists_ShouldReturnToken() {
        // Given
        when(refreshTokenRepository.findByUtilisateur(utilisateur)).thenReturn(Optional.of(refreshToken));

        // When
        Optional<RefreshToken> result = refreshTokenService.findByUtilisateur(utilisateur);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(refreshToken);
        verify(refreshTokenRepository).findByUtilisateur(utilisateur);
    }

    @Test
    @DisplayName("findByUtilisateur - Devrait retourner empty si le token n'existe pas")
    void findByUtilisateur_WhenTokenNotExists_ShouldReturnEmpty() {
        // Given
        when(refreshTokenRepository.findByUtilisateur(utilisateur)).thenReturn(Optional.empty());

        // When
        Optional<RefreshToken> result = refreshTokenService.findByUtilisateur(utilisateur);

        // Then
        assertThat(result).isEmpty();
        verify(refreshTokenRepository).findByUtilisateur(utilisateur);
    }

    @Test
    @DisplayName("generateNewToken - Devrait générer un nouveau token avec succès")
    void generateNewToken_WithValidToken_ShouldGenerateNewToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("refresh-token-123");
        when(refreshTokenRepository.findByToken("refresh-token-123")).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateToken(utilisateur)).thenReturn("new-access-token");

        // When
        RefreshTokenResponse result = refreshTokenService.generateNewToken(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("new-access-token");
        assertThat(result.refreshToken()).isEqualTo("refresh-token-123");
        assertThat(result.tokenType()).isEqualTo(TokenType.BEARER);
        assertThat(result.role()).isEqualTo(Role.BASIC_USER);
        verify(refreshTokenRepository).findByToken("refresh-token-123");
        verify(jwtService).generateToken(utilisateur);
    }

    @Test
    @DisplayName("generateNewToken - Devrait lancer une exception si le token n'existe pas")
    void generateNewToken_WhenTokenNotExists_ShouldThrowException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("non-existent-token");
        when(refreshTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.generateNewToken(request))
            .isInstanceOf(TokenException.class)
            .hasMessageContaining("Refresh token does not exist");
        verify(refreshTokenRepository).findByToken("non-existent-token");
        verify(jwtService, never()).generateToken(any(Utilisateur.class));
    }

    @Test
    @DisplayName("generateNewToken - Devrait lancer une exception si le token est expiré")
    @SuppressWarnings("null")
    void generateNewToken_WithExpiredToken_ShouldThrowException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");
        RefreshToken expiredToken = RefreshToken.builder()
                .id(1L)
                .token("expired-token")
                .utilisateur(utilisateur)
                .expiryDate(Instant.now().minusSeconds(3600))
                .revoked(false)
                .build();
        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));
        doNothing().when(refreshTokenRepository).delete(expiredToken);

        // When & Then
        assertThatThrownBy(() -> refreshTokenService.generateNewToken(request))
            .isInstanceOf(TokenException.class)
            .hasMessageContaining("Refresh token was expired");
        verify(refreshTokenRepository).findByToken("expired-token");
        verify(refreshTokenRepository).delete(expiredToken);
        verify(jwtService, never()).generateToken(any(Utilisateur.class));
    }

    @Test
    @DisplayName("deleteByToken - Devrait supprimer le token s'il existe")
    @SuppressWarnings("null")
    void deleteByToken_WhenTokenExists_ShouldDeleteToken() {
        // Given
        when(refreshTokenRepository.findByToken("refresh-token-123")).thenReturn(Optional.of(refreshToken));
        doNothing().when(refreshTokenRepository).delete(refreshToken);

        // When
        refreshTokenService.deleteByToken("refresh-token-123");

        // Then
        verify(refreshTokenRepository).findByToken("refresh-token-123");
        verify(refreshTokenRepository).delete(refreshToken);
    }

    @Test
    @DisplayName("deleteByToken - Ne devrait rien faire si le token n'existe pas")
    @SuppressWarnings("null")
    void deleteByToken_WhenTokenNotExists_ShouldDoNothing() {
        // Given
        when(refreshTokenRepository.findByToken("non-existent-token")).thenReturn(Optional.empty());

        // When
        refreshTokenService.deleteByToken("non-existent-token");

        // Then
        verify(refreshTokenRepository).findByToken("non-existent-token");
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    @DisplayName("generateRefreshTokenCookie - Devrait générer un cookie avec succès")
    void generateRefreshTokenCookie_WithValidToken_ShouldGenerateCookie() {
        // Given
        String token = "refresh-token-123";

        // When
        var cookie = refreshTokenService.generateRefreshTokenCookie(token);

        // Then
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo("refresh-jwt-cookie");
        assertThat(cookie.getValue()).isEqualTo(token);
        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.getDomain()).isEqualTo("127.0.0.1");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSameSite()).isEqualTo("Lax");
        // Vérifier que le cookie contient Secure dans sa représentation si secureCookie est true
        String cookieString = cookie.toString();
        // En test, secureCookie est false, donc Secure ne devrait pas être présent
        assertThat(cookieString).doesNotContain("Secure");
    }

    @Test
    @DisplayName("getRefreshTokenFromCookies - Devrait retourner le token du cookie")
    @SuppressWarnings("null")
    void getRefreshTokenFromCookies_WhenCookieExists_ShouldReturnToken() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        Cookie cookie = new Cookie("refresh-jwt-cookie", "refresh-token-123");
        try (MockedStatic<org.springframework.web.util.WebUtils> webUtilsMock = mockStatic(org.springframework.web.util.WebUtils.class)) {
            webUtilsMock.when(() -> org.springframework.web.util.WebUtils.getCookie(request, "refresh-jwt-cookie"))
                    .thenReturn(cookie);

            // When
            String result = refreshTokenService.getRefreshTokenFromCookies(request);

            // Then
            assertThat(result).isEqualTo("refresh-token-123");
        }
    }

    @Test
    @DisplayName("getRefreshTokenFromCookies - Devrait retourner une chaîne vide si le cookie n'existe pas")
    @SuppressWarnings("null")
    void getRefreshTokenFromCookies_WhenCookieNotExists_ShouldReturnEmptyString() {
        // Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        try (MockedStatic<org.springframework.web.util.WebUtils> webUtilsMock = mockStatic(org.springframework.web.util.WebUtils.class)) {
            webUtilsMock.when(() -> org.springframework.web.util.WebUtils.getCookie(request, "refresh-jwt-cookie"))
                    .thenReturn(null);

            // When
            String result = refreshTokenService.getRefreshTokenFromCookies(request);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Test
    @DisplayName("getCleanRefreshTokenCookie - Devrait générer un cookie vide pour suppression")
    void getCleanRefreshTokenCookie_ShouldGenerateEmptyCookie() {
        // When
        var cookie = refreshTokenService.getCleanRefreshTokenCookie();

        // Then
        assertThat(cookie).isNotNull();
        assertThat(cookie.getName()).isEqualTo("refresh-jwt-cookie");
        assertThat(cookie.getValue()).isEmpty();
        assertThat(cookie.getPath()).isEqualTo("/");
    }

}
