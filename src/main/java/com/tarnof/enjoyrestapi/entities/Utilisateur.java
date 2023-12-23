package com.tarnof.enjoyrestapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tarnof.enjoyrestapi.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.*;

@Data
@Builder

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
    @ManyToMany
    private List<Sejour> sejours;
    private String nom;
    private String prenom;
    private String genre;
    private String telephone;
    private String email;
    private Date dateNaissance;
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
        return email;
    }
    @Override
    public boolean isAccountNonExpired() {
        Instant expiryDate = refreshToken.getExpiryDate();
        Instant currentDate = Instant.now();
        if(expiryDate.isBefore(currentDate)){
            return false;
        }
            return true;
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
