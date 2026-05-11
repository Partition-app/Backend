package com.partition.domain.user.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.preference.dto.request.UserPreferenceRequest;
import com.partition.domain.preference.service.UserPreferenceService;
import com.partition.domain.user.dto.request.UpdateUserRequest;
import com.partition.domain.user.dto.request.UpdateFcmTokenRequest;
import com.partition.domain.user.service.UserService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserPreferenceService userPreferenceService;


    @Operation(summary = "내 이름(닉네임) 변경", description = "가입 후 이름을 설정할 때 사용합니다.")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<Void>> updateMyName(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateUserRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());

        // 서비스 호출
        userService.updateName(userId, request.getName());

        // 성공 응답 생성
        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        "200",
                        "회원 이름이 정상적으로 변경되었습니다.",
                        null
                )
        );
    }

    @Operation(summary = "집안일 선호도 등록 (온보딩)", description = "온보딩 단계에서 집안일별 점수(1~5)를 저장합니다.")
    @PostMapping("/me/preferences")
    public ResponseEntity<ApiResponse<List<UserPreferenceRequest.PreferenceDto>>> savePreferences(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UserPreferenceRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());

        // 서비스 호출 및 저장된 리스트 반환
        List<UserPreferenceRequest.PreferenceDto> savedPreferences = userPreferenceService.savePreferences(userId, request);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "선호도 등록 성공", savedPreferences)
        );
    }

    @Operation(summary = "FCM 토큰 등록/갱신", description = "앱 실행 시 FCM 토큰을 서버에 등록합니다.")
    @PatchMapping("/me/fcm-token")
    public ResponseEntity<ApiResponse<Void>> updateFcmToken(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateFcmTokenRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        userService.updateFcmToken(userId, request.getFcmToken());

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "FCM 토큰이 등록되었습니다.", null));
    }

    @Operation(summary = "회원탈퇴", description = "카카오 연동을 해제하고 계정을 삭제합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        userService.withdraw(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "회원탈퇴 성공", null));
    }

}