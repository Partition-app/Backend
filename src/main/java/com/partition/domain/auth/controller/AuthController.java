package com.partition.domain.auth.controller;

import com.partition.domain.auth.dto.request.KakaoLoginRequest;
import com.partition.domain.auth.dto.response.TokenResponse;
import com.partition.domain.auth.service.AuthService;
import com.partition.domain.common.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/kakao")
    public ResponseEntity<ApiResponse<TokenResponse>> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        // 서비스 로직 실행
        TokenResponse tokenResponse = authService.kakaoLogin(request);

        // ApiResponse로 감싸서 표준 응답 반환
        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "카카오 로그인 성공", tokenResponse)
        );
    }
}