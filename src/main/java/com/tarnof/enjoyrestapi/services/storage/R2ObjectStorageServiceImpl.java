package com.tarnof.enjoyrestapi.services.storage;

import com.tarnof.enjoyrestapi.config.StorageProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.InputStream;
import java.util.Optional;

@Service
@ConditionalOnProperty(name = "application.storage.r2.enabled", havingValue = "true")
public class R2ObjectStorageServiceImpl implements ObjectStorageService {

    private final S3Client s3Client;
    private final String bucket;

    public R2ObjectStorageServiceImpl(S3Client s3Client, StorageProperties storageProperties) {
        this.s3Client = s3Client;
        this.bucket = storageProperties.getR2().getBucket();
    }

    @Override
    public void upload(String key, InputStream content, long size, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .contentLength(size)
                .build();
        s3Client.putObject(request, RequestBody.fromInputStream(content, size));
    }

    @Override
    public Optional<StoredObject> download(String key) {
        try {
            var response = s3Client.getObject(GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            String contentType = response.response().contentType();
            Long contentLength = response.response().contentLength();
            return Optional.of(new StoredObject(
                    response,
                    contentLength != null ? contentLength : 0L,
                    contentType != null ? contentType : "application/octet-stream"));
        } catch (NoSuchKeyException e) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (S3Exception e) {
            if (e.statusCode() != 404) {
                throw e;
            }
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            s3Client.headObject(HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    @Override
    public String buildPhotoProfilUtilisateurKey(String tokenId, String extension) {
        return "utilisateurs/" + tokenId + "/photo-profil" + extension;
    }
}
