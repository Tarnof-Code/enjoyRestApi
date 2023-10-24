package com.tarnof.enjoyrestapi.auth;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService service;

    @PostMapping("/inscription")
    public ResponseEntity<AuthenticationResponse> inscription (
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    @PostMapping("/connexion")
    public ResponseEntity<AuthenticationResponse> connexion (
            @RequestBody AuthenticationRequest request
    ) {
        System.out.println("++++++++++++++++++ JE SUIS LA+++++++++++++++++");
        return ResponseEntity.ok(service.authenticate(request));

    }

}
