package com.tarnof.enjoyrestapi.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tarnof.enjoyrestapi.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

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

}
/*
public class AuthenticationResponse {
    private Long id;
    private String email;

    //private List<String> roles;
    private Role role;
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("token_type")
    private String tokenType;
}
*/