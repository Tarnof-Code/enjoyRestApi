package com.tarnof.enjoyrestapi.auth;

import com.tarnof.enjoyrestapi.config.JwtService;
import com.tarnof.enjoyrestapi.utilisateur.Role;
import com.tarnof.enjoyrestapi.utilisateur.Utilisateur;
import com.tarnof.enjoyrestapi.utilisateur.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        var utilisateur = Utilisateur.builder()
                .prenom(request.getPrenom())
                .nom(request.getNom())
                .email(request.getEmail())
                .motDePasse(passwordEncoder.encode(request.getMotDePasse()))
                .genre(request.getGenre())
                .dateNaissance(request.getDateNaissance())
                .telephone(request.getTelephone())
                .role(Role.ANIMATEUR)
                .build();
        utilisateurRepository.save(utilisateur);
        var jwtToken = jwtService.generateToken(utilisateur);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()
                )
        );
        var utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(utilisateur);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
