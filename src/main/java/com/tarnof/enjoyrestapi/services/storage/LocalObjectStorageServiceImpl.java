package com.tarnof.enjoyrestapi.services.storage;

import com.tarnof.enjoyrestapi.config.StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "application.storage.r2.enabled", havingValue = "false", matchIfMissing = true)
public class LocalObjectStorageServiceImpl implements ObjectStorageService {

    private final Path uploadDir;

    public LocalObjectStorageServiceImpl(StorageProperties storageProperties) {
        this.uploadDir = Path.of(storageProperties.getLocal().getUploadDir()).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de créer le dossier de stockage local: " + uploadDir, e);
        }
    }

    @Override
    public void upload(String key, InputStream content, long size, String contentType) {
        Path target = resolveKey(key);
        try {
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Impossible d'enregistrer le fichier localement", e);
        }
    }

    @Override
    public Optional<StoredObject> download(String key) {
        Path target = resolveKey(key);
        if (!Files.exists(target)) {
            return Optional.empty();
        }
        try {
            String contentType = Files.probeContentType(target);
            long size = Files.size(target);
            return Optional.of(new StoredObject(
                    Objects.requireNonNull(Files.newInputStream(target)),
                    size,
                    contentType != null ? contentType : "application/octet-stream"));
        } catch (IOException e) {
            throw new RuntimeException("Impossible de lire le fichier localement", e);
        }
    }

    @Override
    public void delete(String key) {
        Path target = resolveKey(key);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            throw new RuntimeException("Impossible de supprimer le fichier localement", e);
        }
    }

    @Override
    public boolean exists(String key) {
        return Files.exists(resolveKey(key));
    }

    @Override
    public String buildPhotoProfilUtilisateurKey(String tokenId, String extension) {
        return "utilisateurs/" + tokenId + "/photo-profil" + extension;
    }

    private Path resolveKey(String key) {
        Path resolved = uploadDir.resolve(key).normalize();
        if (!resolved.startsWith(uploadDir)) {
            throw new IllegalArgumentException("Clé de stockage invalide");
        }
        return resolved;
    }
}
