package com.tarnof.enjoyrestapi.services;

import com.tarnof.enjoyrestapi.payload.request.AuthenticationRequest;
import com.tarnof.enjoyrestapi.payload.request.RegisterRequest;
import com.tarnof.enjoyrestapi.payload.response.AuthenticationResponse;

public interface AuthenticationService {
    AuthenticationResponse register(RegisterRequest request);
    AuthenticationResponse authenticate(AuthenticationRequest request);
}