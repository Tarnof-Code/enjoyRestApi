package com.tarnof.enjoyrestapi.payload.response;

import org.springframework.lang.NonNull;

import java.io.InputStream;

public record PhotoProfilContenu(
        @NonNull InputStream contenu,
        long taille,
        @NonNull String mimeType
) {}
