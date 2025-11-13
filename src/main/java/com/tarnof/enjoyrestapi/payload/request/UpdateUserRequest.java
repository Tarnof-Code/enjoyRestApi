package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String tokenId;
    @NotEmpty(message = "Le champ prénom ne peut pas être vide.")
    private String prenom;
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    private String nom;
    @NotEmpty(message = "Le champ genre ne peut pas être vide.")
    private String genre;
    @NotEmpty(message = "Le champ email ne peut pas être vide.")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email non valide")
    private String email;
    @NotEmpty(message = "Le champ N° de téléphone ne peut pas être vide.")
    @Pattern(regexp = "^0[0-9]{9}$", message = "N° de téléphone non valide")
    private String telephone;
    @Temporal(TemporalType.DATE)
    @NotNull(message = "Le champ date de naissance ne peut pas être vide.")
    private Date dateNaissance;
    @Enumerated(EnumType.STRING)
    private Role role;
    private Instant dateExpirationCompte;

}
