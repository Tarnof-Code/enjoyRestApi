package com.tarnof.enjoyrestapi.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class ObjectStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "application.storage.r2.enabled", havingValue = "true")
    S3Client r2S3Client(StorageProperties storageProperties) {
        StorageProperties.R2 r2 = storageProperties.getR2();
        S3Configuration serviceConfiguration = S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
        return S3Client.builder()
                .endpointOverride(URI.create(r2.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(r2.getAccessKey(), r2.getSecretKey())))
                .region(Region.of("auto"))
                .serviceConfiguration(serviceConfiguration)
                .build();
    }
}
