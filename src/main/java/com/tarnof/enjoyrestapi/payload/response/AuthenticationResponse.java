package com.tarnof.enjoyrestapi.payload.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tarnof.enjoyrestapi.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
    private Role role;
    private String tokenId;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    private String errorMessage;
    public AuthenticationResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
