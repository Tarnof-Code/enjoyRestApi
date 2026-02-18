package com.tarnof.enjoyrestapi.services.impl;
import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.Genre;
import com.tarnof.enjoyrestapi.exceptions.EmailDejaUtiliseException;
import com.tarnof.enjoyrestapi.exceptions.ResourceNotFoundException;
import com.tarnof.enjoyrestapi.payload.request.AuthenticationRequest;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import com.tarnof.enjoyrestapi.payload.response.AuthenticationResponse;
import com.tarnof.enjoyrestapi.repositories.UtilisateurRepository;
import com.tarnof.enjoyrestapi.services.AuthenticationService;
import com.tarnof.enjoyrestapi.services.JwtService;
import com.tarnof.enjoyrestapi.services.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service @Transactional
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    private String generateTokenId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        if(utilisateurRepository.existsByEmail(request.email())){
            throw new EmailDejaUtiliseException("Un compte avec cette adresse e-mail existe déjà.");
        }
        if(request.dateExpiration() == null) {
            throw new IllegalArgumentException("La date d'expiration est obligatoire pour l'inscription.");
        }
        var utilisateur = Utilisateur.builder()
                .prenom(request.prenom())
                .nom(request.nom())
                .email(request.email())
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .genre(Genre.parseGenre(request.genre()))
                .dateNaissance(request.dateNaissance())
                .telephone(request.telephone())
                .role(request.role())
                .tokenId(generateTokenId())
                .build();      
        Objects.requireNonNull(utilisateur, "L'utilisateur n'a pas pu être sauvegardé");
        utilisateur =  utilisateurRepository.save(utilisateur);
        var jwt = jwtService.generateToken(utilisateur);
        var refreshToken = refreshTokenService.createRefreshToken(utilisateur.getId(),request.dateExpiration());
        var role = utilisateur.getRole();
        return new AuthenticationResponse(
            role,
            utilisateur.getTokenId(),
            jwt,
            refreshToken.getToken(),
            null // pas d'erreur
        );
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(),request.motDePasse()));
        var utilisateur = utilisateurRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable."));
        var role = utilisateur.getRole();
        var jwt = jwtService.generateToken(utilisateur);
        var refreshTokenValue = refreshTokenService.findByUtilisateur(utilisateur)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token introuvable"));

        return new AuthenticationResponse(
            role,
            utilisateur.getTokenId(),
            jwt,
            refreshTokenValue.getToken(),
            null // pas d'erreur
        );

    }
}
