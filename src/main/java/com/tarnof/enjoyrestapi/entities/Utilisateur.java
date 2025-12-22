package com.tarnof.enjoyrestapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tarnof.enjoyrestapi.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Utilisateur implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(unique = true)
    private String tokenId;
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private List<SejourEquipe> sejoursEquipe;
    @NotEmpty(message = "Le champ nom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    private String nom;
    @NotEmpty(message = "Le champ prénom ne peut pas être vide.")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ]+(([',. -][a-zA-ZÀ-ÿ ])?[a-zA-ZÀ-ÿ]*)*$", message = "Caractères non autorisés")
    private String prenom;
    @NotEmpty(message = "Le champ genre ne peut pas être vide.")
    private String genre;
    @Column(unique = true)
    @NotEmpty(message = "Le champ N° de téléphone ne peut pas être vide.")
    @Pattern(regexp = "^0[0-9]{9}$", message = "Numéro de téléphone non valide")
    private String telephone;
    @Column(unique = true)
    @NotEmpty(message = "Le champ email ne peut pas être vide.")
    @Pattern(regexp = "^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", message = "Email non valide")
    private String email;
    @Temporal(TemporalType.DATE)
    @NotNull(message = "Le champ date de naissance ne peut pas être vide.")
    private Date dateNaissance;
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&*!]).{4,}$", message = "Le mot de passe doit contenir au moins une minuscule, une majuscule, et un caractère spécial, et comporter au moins 4 caractères")
    private String motDePasse;
    @Transient
    private Instant dateExpirationCompte;
    @JsonIgnore
    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private RefreshToken refreshToken;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getPassword() { return motDePasse; }
    @Override
    public String getUsername() {
        return tokenId;
    }
    @Override
    public boolean isAccountNonExpired() {
        Instant expiryDate = refreshToken.getExpiryDate();
        Instant currentDate = Instant.now();
        return !expiryDate.isBefore(currentDate);
    }

    @Transient
    public Instant getDateExpirationCompte(){
        if (refreshToken != null && refreshToken.getExpiryDate() != null) {
            return refreshToken.getExpiryDate();
        }
        return null;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }
}
