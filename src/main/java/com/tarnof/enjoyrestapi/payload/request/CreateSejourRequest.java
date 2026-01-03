package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.*;

import java.util.Date;

public record CreateSejourRequest(
    @NotBlank(message = "Le nom du séjour est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    String nom,
    
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 500, message = "La description doit contenir entre 10 et 500 caractères")
    String description,
    
    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    Date dateDebut,
    
    @NotNull(message = "La date de fin est obligatoire")
    @Future(message = "La date de fin doit être dans le futur")
    Date dateFin,
    
    @NotBlank(message = "Le lieu du séjour est obligatoire")
    @Size(min = 2, max = 100, message = "Le lieu doit contenir entre 2 et 100 caractères")
    String lieuDuSejour,

    String directeurTokenId
) {}
