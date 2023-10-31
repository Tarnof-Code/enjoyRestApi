package com.tarnof.enjoyrestapi.controllers;


import com.tarnof.enjoyrestapi.payload.request.AuthenticationRequest;
import com.tarnof.enjoyrestapi.payload.request.RefreshTokenRequest;
import com.tarnof.enjoyrestapi.payload.response.AuthenticationResponse;
import com.tarnof.enjoyrestapi.payload.response.RefreshTokenResponse;
import com.tarnof.enjoyrestapi.services.AuthenticationService;
import com.tarnof.enjoyrestapi.services.RefreshTokenService;
import com.tarnof.enjoyrestapi.services.impl.AuthenticationServiceImpl;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/inscription")
    public ResponseEntity<AuthenticationResponse> inscription (
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/connexion")
    public ResponseEntity<AuthenticationResponse> connexion (
            @RequestBody AuthenticationRequest request
    ) {
        System.out.println("++++++++++++++++++ Auth controller+++++++++++++++++");
        System.out.println(request);
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.generateNewToken(request));
    }

}
