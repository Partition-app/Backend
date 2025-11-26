package com.partition.domain.auth.dto.response;

import com.partition.entity.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class TokenResponse {
    private String grantType;    // "Bearer"
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
    private UserRole userRole;

}