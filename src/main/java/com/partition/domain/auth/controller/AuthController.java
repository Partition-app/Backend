package com.partition.domain.auth.controller;

import com.partition.domain.auth.dto.request.KakaoLoginRequest;
import com.partition.domain.auth.dto.response.TokenResponse;
import com.partition.domain.auth.service.AuthService;
import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "카카오 로그인", description = "카카오 액세스 토큰으로 로그인 또는 회원가입 처리 후 JWT를 발급합니다.")
    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        TokenResponse tokenResponse = authService.kakaoLogin(request);
        return ResponseEntity.ok(ApiResponse.onSuccess("200", "카카오 로그인 성공", tokenResponse));
    }

    @Operation(summary = "로그아웃", description = "로그아웃 처리합니다. 클라이언트에서 저장된 토큰을 삭제해야 합니다.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.onSuccess("200", "로그아웃 성공"));
    }
}