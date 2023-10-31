package com.tarnof.enjoyrestapi.payload.request;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String refreshToken;
}