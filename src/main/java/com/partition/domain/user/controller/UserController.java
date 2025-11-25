package com.partition.domain.user.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.user.dto.request.UpdateUserRequest;
import com.partition.domain.user.service.UserService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
                        "USER_2001",
                        "회원 이름이 정상적으로 변경되었습니다.",
                        null
                )
        );
    }

}