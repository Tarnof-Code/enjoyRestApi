package com.tarnof.enjoyrestapi.services.storage;

import org.springframework.lang.NonNull;

import java.io.InputStream;
import java.util.Optional;

/**
 * Abstraction du stockage objet (Cloudflare R2 en prod, disque local en dev).
 * Les clés suivent la convention {@code utilisateurs/{tokenId}/photo-profil.ext}
 * ou, à terme, {@code enfants/{enfantId}/photo-profil.ext}.
 */
public interface ObjectStorageService {

    void upload(String key, InputStream content, long size, String contentType);

    Optional<StoredObject> download(String key);

    void delete(String key);

    boolean exists(String key);

    String buildPhotoProfilUtilisateurKey(String tokenId, String extension);

    record StoredObject(@NonNull InputStream content, long size, @NonNull String contentType) {}
}
