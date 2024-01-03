package com.tarnof.enjoyrestapi.payload.request;

import com.tarnof.enjoyrestapi.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    private String tokenId;
    private String prenom;
    private String nom;
    private String genre;
    @Email(message = "email format is not valid")
    private String email;
    private String telephone;
    private Date dateNaissance;
}
