package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tarnof.enjoyrestapi.enums.Role;
import com.tarnof.enjoyrestapi.enums.TokenType;

public record RefreshTokenResponse(
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("refresh_token")
    String refreshToken,
    @JsonProperty("token_type")
    TokenType tokenType,
    Role role
) {}
