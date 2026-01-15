package com.tarnof.enjoyrestapi.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Objects;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour JwtServiceImpl")
public class JwtServiceImplTest {

    private JwtServiceImpl jwtServiceImpl;
    
    @Mock
    private UserDetails userDetails;

    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        // Créer une instance du service
        jwtServiceImpl = new JwtServiceImpl();
        
        // Lire les propriétés depuis application-test.yml
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-test.yml"));
        Properties properties = Objects.requireNonNull(yaml.getObject(), 
            "Les propriétés de test n'ont pas pu être chargées depuis application-test.yml");
        
        // Injecter les valeurs avec ReflectionTestUtils
        String secretKey = properties.getProperty("application.security.jwt.secret-key");
        String expiration = properties.getProperty("application.security.jwt.expiration");
        
        assertThat(secretKey).isNotNull();
        assertThat(expiration).isNotNull();
        
        ReflectionTestUtils.setField(jwtServiceImpl, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtServiceImpl, "jwtExpiration", Long.parseLong(expiration));
        
        // Configurer le mock UserDetails (lenient pour éviter les stubbings inutiles)
        lenient().when(userDetails.getUsername()).thenReturn("test@example.com");
    }

    @Test
    @DisplayName("extractUserName - Devrait extraire le nom d'utilisateur d'un token valide")
    void extractUserName_WithValidToken_ShouldExtractUsername() {
        // Given
        String token = jwtServiceImpl.generateToken(userDetails); 
        // When
        String extractedUsername = jwtServiceImpl.extractUserName(token);     
        // Then
        assertThat(extractedUsername).isNotNull();
        assertThat(extractedUsername).isEqualTo("test@example.com");
        verify(userDetails).getUsername();
    }

    @Test
    @DisplayName("generateToken - Devrait générer un token JWT valide")
    void generateToken_WithValidUserDetails_ShouldGenerateToken() {
        // Given
        // userDetails déjà configuré dans setUp()     
        // When
        String token = jwtServiceImpl.generateToken(userDetails);        
        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        String extractedUsername = jwtServiceImpl.extractUserName(token);
        assertThat(extractedUsername).isEqualTo("test@example.com");
        verify(userDetails).getUsername();
    }

    @Test
    @DisplayName("generateToken - Devrait générer des tokens avec le même username")
    void generateToken_WithSameUserDetails_ShouldGenerateTokensWithSameUsername() throws Exception {
        // Given
        // userDetails déjà configuré dans setUp()      
        // When
        String token1 = jwtServiceImpl.generateToken(userDetails);
        Thread.sleep(10);
        String token2 = jwtServiceImpl.generateToken(userDetails);     
        // Then
        assertThat(jwtServiceImpl.extractUserName(token1)).isEqualTo("test@example.com");
        assertThat(jwtServiceImpl.extractUserName(token2)).isEqualTo("test@example.com");
        assertThat(jwtServiceImpl.isTokenValid(token1, userDetails)).isTrue();
        assertThat(jwtServiceImpl.isTokenValid(token2, userDetails)).isTrue();
        verify(userDetails, atLeast(2)).getUsername();
    }

    @Test
    @DisplayName("isTokenValid - Devrait retourner true pour un token valide")
    void isTokenValid_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtServiceImpl.generateToken(userDetails);    
        // When
        boolean isValid = jwtServiceImpl.isTokenValid(token, userDetails);   
        // Then
        assertThat(isValid).isTrue();
        verify(userDetails, atLeast(2)).getUsername();
    }

    @Test
    @DisplayName("isTokenValid - Devrait retourner false pour un token avec un mauvais username")
    void isTokenValid_WithWrongUsername_ShouldReturnFalse() {
        // Given
        String token = jwtServiceImpl.generateToken(userDetails);
        UserDetails wrongUserDetails = mock(UserDetails.class);
        when(wrongUserDetails.getUsername()).thenReturn("wrong@example.com");    
        // When
        boolean isValid = jwtServiceImpl.isTokenValid(token, wrongUserDetails);      
        // Then
        assertThat(isValid).isFalse();
        verify(wrongUserDetails).getUsername();
    }

    @Test
    @DisplayName("isTokenValid - Devrait lancer une exception pour un token expiré")
    @SuppressWarnings("null")
    void isTokenValid_WithExpiredToken_ShouldThrowException() throws Exception {
        // Given
        ReflectionTestUtils.setField(jwtServiceImpl, "jwtExpiration", 1L);
        String token = jwtServiceImpl.generateToken(userDetails);
        ReflectionTestUtils.setField(jwtServiceImpl, "jwtExpiration", 10800000L);
        Thread.sleep(10);     
        // When & Then
        assertThatThrownBy(() -> jwtServiceImpl.isTokenValid(token, userDetails))
            .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        verify(userDetails).getUsername();
    }

    @Test
    @DisplayName("isTokenValid - Devrait lancer une exception pour un token invalide")
    void isTokenValid_WithInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.token.here";     
        // When & Then
        assertThatThrownBy(() -> jwtServiceImpl.isTokenValid(invalidToken, userDetails))
            .isInstanceOfAny(
                io.jsonwebtoken.security.SignatureException.class,
                io.jsonwebtoken.MalformedJwtException.class,
                io.jsonwebtoken.security.InvalidKeyException.class
            );
        // Note: userDetails.getUsername() n'est pas appelé car l'exception est levée avant lors de l'extraction du username
    }

}
