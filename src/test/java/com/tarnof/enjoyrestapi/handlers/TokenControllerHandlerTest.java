package com.tarnof.enjoyrestapi.handlers;

import com.tarnof.enjoyrestapi.exceptions.TokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour TokenControllerHandler")
class TokenControllerHandlerTest {

    private MockMvc mockMvc;
    private TokenControllerHandler tokenControllerHandler;

    // Contrôleur de test pour déclencher les exceptions
    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/token-exception")
        public void testTokenException() {
            throw new TokenException("user@test.fr", "Token invalide ou expiré");
        }
    }

    @BeforeEach
    void setUp() {
        tokenControllerHandler = new TokenControllerHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(tokenControllerHandler)
                .build();
    }

    // ========== Tests pour handleRefreshTokenException() ==========

    @Test
    @DisplayName("handleRefreshTokenException - Devrait retourner 403 Forbidden avec ErrorResponse structuré")
    void handleRefreshTokenException_ShouldReturn403Forbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/token-exception"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Invalid Token"))
                .andExpect(jsonPath("$.message").value("Failed for [user@test.fr]: Token invalide ou expiré"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").exists());
    }

    @Test
    @DisplayName("handleRefreshTokenException - Devrait retourner le format ErrorResponse correct")
    void handleRefreshTokenException_ShouldReturnCorrectErrorResponse() {
        // Given
        TokenException exception = new TokenException("user@test.fr", "Token invalide ou expiré");
        ServletWebRequest webRequest = createMockWebRequest("/api/v1/auth/refresh-token");

        // When
        ResponseEntity<ErrorResponse> response = tokenControllerHandler.handleRefreshTokenException(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        ErrorResponse errorResponse = Objects.requireNonNull(response.getBody(), "ErrorResponse ne doit pas être null");
        assertThat(errorResponse.getStatus()).isEqualTo(403);
        assertThat(errorResponse.getError()).isEqualTo("Invalid Token");
        assertThat(errorResponse.getMessage()).isEqualTo("Failed for [user@test.fr]: Token invalide ou expiré");
        assertThat(errorResponse.getTimestamp()).isNotNull();
        assertThat(errorResponse.getPath()).isEqualTo("uri=/api/v1/auth/refresh-token");
    }

    @Test
    @DisplayName("handleRefreshTokenException - Devrait inclure le timestamp dans la réponse")
    void handleRefreshTokenException_ShouldIncludeTimestamp() {
        // Given
        TokenException exception = new TokenException("user@test.fr", "Token expiré");
        ServletWebRequest webRequest = createMockWebRequest("/api/v1/auth/refresh-token");

        // When
        ResponseEntity<ErrorResponse> response = tokenControllerHandler.handleRefreshTokenException(exception, webRequest);

        // Then
        ErrorResponse errorResponse = Objects.requireNonNull(response.getBody(), "ErrorResponse ne doit pas être null");
        assertThat(errorResponse.getTimestamp()).isNotNull();
        // Vérifier que le timestamp est récent (dans les dernières secondes)
        assertThat(errorResponse.getTimestamp()).isBefore(java.time.Instant.now().plusSeconds(1));
    }

    @Test
    @DisplayName("handleRefreshTokenException - Devrait inclure le path correct dans la réponse")
    void handleRefreshTokenException_ShouldIncludeCorrectPath() {
        // Given
        TokenException exception = new TokenException("user@test.fr", "Token invalide");
        ServletWebRequest webRequest = createMockWebRequest("/api/v1/auth/refresh-token");

        // When
        ResponseEntity<ErrorResponse> response = tokenControllerHandler.handleRefreshTokenException(exception, webRequest);

        // Then
        ErrorResponse errorResponse = Objects.requireNonNull(response.getBody(), "ErrorResponse ne doit pas être null");
        assertThat(errorResponse.getPath()).isEqualTo("uri=/api/v1/auth/refresh-token");
    }

    @Test
    @DisplayName("handleRefreshTokenException - Devrait formater correctement le message avec email et message")
    void handleRefreshTokenException_ShouldFormatMessageCorrectly() {
        // Given
        TokenException exception = new TokenException("admin@test.fr", "Refresh token non trouvé");
        ServletWebRequest webRequest = createMockWebRequest("/api/v1/auth/refresh-token");

        // When
        ResponseEntity<ErrorResponse> response = tokenControllerHandler.handleRefreshTokenException(exception, webRequest);

        // Then
        ErrorResponse errorResponse = Objects.requireNonNull(response.getBody(), "ErrorResponse ne doit pas être null");
        assertThat(errorResponse.getMessage()).isEqualTo("Failed for [admin@test.fr]: Refresh token non trouvé");
    }

    /**
     * Crée un mock ServletWebRequest pour les tests
     */
    private ServletWebRequest createMockWebRequest(String requestUri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(requestUri);
        return new ServletWebRequest(request);
    }
}
