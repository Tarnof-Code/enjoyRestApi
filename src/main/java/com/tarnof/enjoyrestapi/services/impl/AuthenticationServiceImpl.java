package com.tarnof.enjoyrestapi.services.impl;


import com.tarnof.enjoyrestapi.entities.Utilisateur;
import com.tarnof.enjoyrestapi.enums.TokenType;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        var utilisateur = Utilisateur.builder()
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .genre(request.getGenre())
                .dateNaissance(request.getDateNaissance())
                .telephone(request.getTelephone())
                .role(request.getRole())
                .build();
        utilisateur = utilisateurRepository.save(utilisateur);
        var jwt = jwtService.generateToken(utilisateur);
        var refreshToken = refreshTokenService.createRefreshToken(utilisateur.getId());

    /*    var roles = utilisateur.getRole().getAuthorities()
                .stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .toList();
    */
        var role = utilisateur.getRole();

        return AuthenticationResponse.builder()
                .accessToken(jwt)
                .email(utilisateur.getEmail())
                .id(Long.valueOf(utilisateur.getId()))
                .refreshToken(refreshToken.getToken())
                .role(role)
                .tokenType( TokenType.BEARER.name())
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(),request.getMotDePasse()));

        var utilisateur = utilisateurRepository.findByEmail(request.getEmail()).orElseThrow(() -> new IllegalArgumentException("Invalid email   or password."));
        /*var roles = utilisateur.getRole().getAuthorities()
                .stream()
                .map(SimpleGrantedAuthority::getAuthority)
                .toList();

         */
        var role = utilisateur.getRole();
        var jwt = jwtService.generateToken(utilisateur);
        var refreshTokenValue = refreshTokenService.findByUtilisateur(utilisateur)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token introuvable"));

        return AuthenticationResponse.builder()
                .accessToken(jwt)
                .role(role)
                .email(utilisateur.getEmail())
                .id(Long.valueOf(utilisateur.getId()))
                .refreshToken(refreshTokenValue.getToken())
                .tokenType( TokenType.BEARER.name())
                .build();
    }
}
