package com.tarnof.enjoyrestapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tarnof.enjoyrestapi.handlers.ErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);

    /**
     * Création / mise à jour / suppression de contenu séjour (hors opérations réservées à l’admin global du tenant).
     */
    static final String MSG_GESTION_SEJOUR_RESERVEE_DIR_ADJ =
            "Cette opération est réservée au directeur de séjour ou à un adjoint disposant des droits de gestion.";

    static final String MSG_ADMIN_SEJOUR_GLOBAL =
            "Cette action est réservée aux administrateurs de la plateforme.";

    static final String MSG_REFERENCES_ALIMENTAIRES_GLOBALES =
            "L’accès à ce référentiel alimentaire est réservé aux administrateurs ou aux utilisateurs avec le profil direction.";

    static final String MSG_GESTION_UTILISATEURS =
            "Vous n’avez pas les droits nécessaires pour gérer les comptes utilisateurs.";

    static final String MSG_RECHERCHE_UTILISATEUR_EQUIPE =
            "La recherche d’utilisateur en vue d’un ajout à l’équipe est réservée au directeur ou à l’administrateur.";

    static final String MSG_ACCES_INTERDIT_GENERIQUE =
            "Accès refusé : vous ne disposez pas des autorisations requises pour cette ressource.";

    static final String MSG_SEJOURS_PAR_UTILISATEUR =
            "Vous n’avez pas l’autorisation de consulter les séjours associés à cet utilisateur.";

    static final String MSG_DOSSIER_SANITAIRE =
            "La modification du dossier santé d’un enfant est réservée aux membres avec le rôle sanitaire (AS), "
                    + "aux adjoints, au directeur de séjour ou à un profil disposant des mêmes droits. "
                    + "Votre rôle actuel ne permet pas cette action.";

    private static final Pattern ADMIN_SEJOUR_URI =
            Pattern.compile(".*/sejours/?$|.*/sejours/\\d+/?$");

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String resolvedMessage = resolveMessageForClient(request, accessDeniedException);
        log.warn("Access denied [{}]: {} — client message: {}", request.getMethod(), accessDeniedException.getMessage(), resolvedMessage);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ErrorResponse body = ErrorResponse.builder()
                .status(HttpServletResponse.SC_FORBIDDEN)
                .error("Forbidden")
                .timestamp(Instant.now())
                .message(resolvedMessage)
                .path(request.getServletPath())
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        mapper.writeValue(response.getOutputStream(), body);
    }

    public static String resolveMessageForClient(HttpServletRequest request, AccessDeniedException accessDeniedException) {
        String original = accessDeniedException.getMessage();
        if (!isUninformativeAccessDeniedMessage(original)) {
            return original;
        }
        String uri = request.getRequestURI();
        if (uri == null) {
            return MSG_ACCES_INTERDIT_GENERIQUE;
        }
        String path = uri.contains("?") ? uri.substring(0, uri.indexOf('?')) : uri;
        if (ADMIN_SEJOUR_URI.matcher(path).matches()) {
            return MSG_ADMIN_SEJOUR_GLOBAL;
        }
        if (path.contains("/references-alimentaires") && !path.contains("/sejours/")) {
            return MSG_REFERENCES_ALIMENTAIRES_GLOBALES;
        }
        if (path.contains("/utilisateurs")) {
            if (path.contains("/search")) {
                return MSG_RECHERCHE_UTILISATEUR_EQUIPE;
            }
            return MSG_GESTION_UTILISATEURS;
        }
        if (path.contains("/sejours/utilisateur/")) {
            return MSG_SEJOURS_PAR_UTILISATEUR;
        }
        if ("PUT".equalsIgnoreCase(request.getMethod())
                && path.contains("/enfants/")
                && path.contains("/dossier")) {
            return MSG_DOSSIER_SANITAIRE;
        }
        if (path.contains("/sejours/") || path.endsWith("/sejours")) {
            return MSG_GESTION_SEJOUR_RESERVEE_DIR_ADJ;
        }
        return MSG_ACCES_INTERDIT_GENERIQUE;
    }

    /**
     * Messages Spring Security par défaut ({@code @PreAuthorize}) sans détail exploitable pour le client.
     */
    public static boolean isUninformativeAccessDeniedMessage(String message) {
        if (message == null || message.isBlank()) {
            return true;
        }
        String m = message.trim().toLowerCase(Locale.ROOT);
        return "access denied".equals(m)
                || "access is denied".equals(m)
                || m.startsWith("access is denied");
    }
}