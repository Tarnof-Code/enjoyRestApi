package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tarnof.enjoyrestapi.enums.Role;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthenticationResponse(
    Role role,
    String tokenId,
    @JsonProperty("access_token")
    String accessToken,
    @JsonProperty("refresh_token")
    String refreshToken,
    String errorMessage
) {
    // Factory method pour créer une réponse d'erreur
    public static AuthenticationResponse error(String errorMessage) {
        return new AuthenticationResponse(null, null, null, null, errorMessage);
    }
}
