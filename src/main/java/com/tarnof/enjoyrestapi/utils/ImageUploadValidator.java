package com.tarnof.enjoyrestapi.utils;

import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public final class ImageUploadValidator {

    private static final long MAX_PHOTO_PROFIL_OCTETS = 2L * 1024 * 1024;
    private static final Set<String> MIME_TYPES_AUTORISES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private ImageUploadValidator() {}

    public static void validerPhotoProfil(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("La photo est vide");
        }
        if (file.getSize() > MAX_PHOTO_PROFIL_OCTETS) {
            throw new IllegalArgumentException("La photo ne doit pas dépasser 2 Mo");
        }
        String contentType = file.getContentType();
        if (contentType == null || !MIME_TYPES_AUTORISES.contains(contentType)) {
            throw new IllegalArgumentException("Format non autorisé. Utilisez JPEG, PNG ou WebP");
        }
    }

    public static String extensionDepuisMimeType(String mimeType) {
        return switch (mimeType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Format non autorisé. Utilisez JPEG, PNG ou WebP");
        };
    }
}
