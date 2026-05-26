package com.example.identityservice.dto;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtResponse {
    private String accessToken;
    private String refreshToken;
    private final String type = "Bearer";
    private Date accessTokenExpiration;
    private Date refreshTokenExpiration;
}
