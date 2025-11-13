package com.tarnof.enjoyrestapi.payload.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateSejourRequest {
    @NotBlank(message = "Le nom du séjour est obligatoire")
    @Size(min = 2, max = 100, message = "Le nom doit contenir entre 2 et 100 caractères")
    private String nom;
    
    @NotBlank(message = "La description est obligatoire")
    @Size(min = 10, max = 500, message = "La description doit contenir entre 10 et 500 caractères")
    private String description;
    
    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    private Date dateDebut;
    
    @NotNull(message = "La date de fin est obligatoire")
    @Future(message = "La date de fin doit être dans le futur")
    private Date dateFin;
    
    @NotBlank(message = "Le lieu du séjour est obligatoire")
    @Size(min = 2, max = 100, message = "Le lieu doit contenir entre 2 et 100 caractères")
    private String lieuDuSejour;

    @NotBlank(message = "Le directeur est obligatoire")
    private String directeurTokenId;
}
