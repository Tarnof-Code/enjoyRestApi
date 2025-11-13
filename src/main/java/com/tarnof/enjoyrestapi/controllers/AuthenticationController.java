package com.tarnof.enjoyrestapi.controllers;


import com.tarnof.enjoyrestapi.payload.request.AuthenticationRequest;
import com.tarnof.enjoyrestapi.payload.request.RefreshTokenRequest;
import com.tarnof.enjoyrestapi.payload.response.AuthenticationResponse;
import com.tarnof.enjoyrestapi.payload.response.RefreshTokenResponse;
import com.tarnof.enjoyrestapi.services.AuthenticationService;
import com.tarnof.enjoyrestapi.services.RefreshTokenService;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/inscription")
    public ResponseEntity<AuthenticationResponse> inscription(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthenticationResponse authenticationResponse = authenticationService.register(request);
            ResponseCookie refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie(authenticationResponse.getRefreshToken());
            // Sécurité : on retire le refreshToken du body pour éviter l'exposition en JavaScript
            authenticationResponse.setRefreshToken(null);
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                    .body(authenticationResponse);
        } catch (ValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthenticationResponse(e.getMessage()));
        }
    }

    @PostMapping("/connexion")
    public ResponseEntity<AuthenticationResponse> connexion (@RequestBody AuthenticationRequest request) {
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(request);
        ResponseCookie refreshTokenCookie = refreshTokenService.generateRefreshTokenCookie(authenticationResponse.getRefreshToken());
       System.out.println("refreshTokenCookie: " + refreshTokenCookie);
        // Sécurité : on retire le refreshToken du body pour éviter l'exposition en JavaScript
        authenticationResponse.setRefreshToken(null);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(authenticationResponse);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(HttpServletRequest request) {
        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        return ResponseEntity.ok(refreshTokenService.generateNewToken(new RefreshTokenRequest(refreshToken)));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request){
        String refreshToken = refreshTokenService.getRefreshTokenFromCookies(request);
        if(refreshToken != null) {
            refreshTokenService.deleteByToken(refreshToken);
        }
        ResponseCookie refreshTokenCookie = refreshTokenService.getCleanRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,refreshTokenCookie.toString())
                .build();

    }
}
