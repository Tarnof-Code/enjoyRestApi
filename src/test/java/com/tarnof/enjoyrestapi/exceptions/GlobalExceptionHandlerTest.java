package com.tarnof.enjoyrestapi.exceptions;

import com.tarnof.enjoyrestapi.handlers.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests unitaires pour GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private GlobalExceptionHandler globalExceptionHandler;

    // Contrôleur de test pour déclencher les exceptions
    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/resource-already-exists")
        public void testResourceAlreadyExists() {
            throw new ResourceAlreadyExistsException("Ressource déjà existante");
        }

        @GetMapping("/email-deja-utilise")
        public void testEmailDejaUtilise() {
            throw new EmailDejaUtiliseException("Cet email est déjà utilisé");
        }

        @GetMapping("/utilisateur-exception")
        public void testUtilisateurException() {
            throw new UtilisateurException("Erreur utilisateur");
        }

        @GetMapping("/resource-not-found")
        public void testResourceNotFound() {
            throw new ResourceNotFoundException("Ressource non trouvée");
        }

        @GetMapping("/runtime-exception")
        public void testRuntimeException() {
            throw new RuntimeException("Erreur runtime");
        }

        @GetMapping("/illegal-argument")
        public void testIllegalArgumentException() {
            throw new IllegalArgumentException("Argument invalide");
        }
    }

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();

        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(globalExceptionHandler)
                .build();
    }

    // ========== Tests pour handleValidationExceptions() ==========

    @Test
    @DisplayName("handleValidationExceptions - Devrait retourner 400 Bad Request avec les détails des erreurs de validation")
    @SuppressWarnings("null")
    void handleValidationExceptions_ShouldReturn400WithValidationErrors() throws Exception {
        // Given - Créer une exception de validation manuellement
        // Utiliser une méthode réelle pour créer un MethodParameter valide
        Method testMethod = TestController.class.getMethod("testResourceNotFound");
        MethodParameter methodParameter = new MethodParameter(testMethod, -1);
        
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testRequest");
        bindingResult.addError(new FieldError("testRequest", "field1", null, false, null, null, "Le champ est obligatoire"));
        bindingResult.addError(new FieldError("testRequest", "email", null, false, null, null, "Email invalide"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleValidationExceptions(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) response.getBody();
        assertThat(errors).isNotNull();
        assertThat(errors).hasSize(2);
        assertThat(errors).containsEntry("field1", "Le champ est obligatoire");
        assertThat(errors).containsEntry("email", "Email invalide");
    }

    // ========== Tests pour handleResourceAlreadyExistsException() ==========

    @Test
    @DisplayName("handleResourceAlreadyExistsException - Devrait retourner 409 Conflict")
    void handleResourceAlreadyExistsException_ShouldReturn409Conflict() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/resource-already-exists"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Ressource déjà existante"));
    }

    @Test
    @DisplayName("handleResourceAlreadyExistsException - Devrait retourner le message d'erreur correct")
    void handleResourceAlreadyExistsException_ShouldReturnCorrectErrorMessage() {
        // Given
        ResourceAlreadyExistsException exception = new ResourceAlreadyExistsException("Ressource déjà existante");

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleResourceAlreadyExistsException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertThat(error).isNotNull();
        assertThat(error).containsEntry("error", "Ressource déjà existante");
    }

    // ========== Tests pour handleEmailDejaUtiliseException() ==========

    @Test
    @DisplayName("handleEmailDejaUtiliseException - Devrait retourner 409 Conflict")
    void handleEmailDejaUtiliseException_ShouldReturn409Conflict() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/email-deja-utilise"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Cet email est déjà utilisé"));
    }

    @Test
    @DisplayName("handleEmailDejaUtiliseException - Devrait retourner le message d'erreur correct")
    void handleEmailDejaUtiliseException_ShouldReturnCorrectErrorMessage() {
        // Given
        EmailDejaUtiliseException exception = new EmailDejaUtiliseException("Cet email est déjà utilisé");

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleEmailDejaUtiliseException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(409);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertThat(error).isNotNull();
        assertThat(error).containsEntry("error", "Cet email est déjà utilisé");
    }

    // ========== Tests pour handleUtilisateurException() ==========

    @Test
    @DisplayName("handleUtilisateurException - Devrait retourner 400 Bad Request")
    void handleUtilisateurException_ShouldReturn400BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/utilisateur-exception"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Erreur utilisateur"));
    }

    @Test
    @DisplayName("handleUtilisateurException - Devrait retourner le message d'erreur correct")
    void handleUtilisateurException_ShouldReturnCorrectErrorMessage() {
        // Given
        UtilisateurException exception = new UtilisateurException("Erreur utilisateur");

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleUtilisateurException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertThat(error).isNotNull();
        assertThat(error).containsEntry("error", "Erreur utilisateur");
    }

    // ========== Tests pour handleResourceNotFoundException() ==========

    @Test
    @DisplayName("handleResourceNotFoundException - Devrait retourner 404 Not Found")
    void handleResourceNotFoundException_ShouldReturn404NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/resource-not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Ressource non trouvée"));
    }

    @Test
    @DisplayName("handleResourceNotFoundException - Devrait retourner le message d'erreur correct")
    void handleResourceNotFoundException_ShouldReturnCorrectErrorMessage() {
        // Given
        ResourceNotFoundException exception = new ResourceNotFoundException("Ressource non trouvée");

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleResourceNotFoundException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertThat(error).isNotNull();
        assertThat(error).containsEntry("error", "Ressource non trouvée");
    }

    // ========== Tests pour handleRuntimeException() ==========

    @Test
    @DisplayName("handleRuntimeException - Devrait retourner 404 Not Found")
    void handleRuntimeException_ShouldReturn404NotFound() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/runtime-exception"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Erreur runtime"));
    }

    @Test
    @DisplayName("handleRuntimeException - Devrait retourner le message d'erreur correct")
    void handleRuntimeException_ShouldReturnCorrectErrorMessage() {
        // Given
        RuntimeException exception = new RuntimeException("Erreur runtime");

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleRuntimeException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertThat(error).isNotNull();
        assertThat(error).containsEntry("error", "Erreur runtime");
    }

    // ========== Tests pour handleIllegalArgumentException() ==========

    @Test
    @DisplayName("handleIllegalArgumentException - Devrait retourner 400 Bad Request")
    void handleIllegalArgumentException_ShouldReturn400BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/test/illegal-argument"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Argument invalide"));
    }

    @Test
    @DisplayName("handleIllegalArgumentException - Devrait retourner le message d'erreur correct")
    void handleIllegalArgumentException_ShouldReturnCorrectErrorMessage() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Argument invalide");

        // When
        ResponseEntity<?> response = globalExceptionHandler.handleIllegalArgumentException(exception);

        // Then
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isInstanceOf(Map.class);
        
        @SuppressWarnings("unchecked")
        Map<String, String> error = (Map<String, String>) response.getBody();
        assertThat(error).isNotNull();
        assertThat(error).containsEntry("error", "Argument invalide");
    }
}
