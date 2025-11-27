package com.partition.domain.chore.controller;

import com.partition.domain.chore.service.ChoreAssignmentService;
import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@RestController
@RequestMapping("/api/chores")
@RequiredArgsConstructor
public class ChoreController {

    private final ChoreAssignmentService assignmentService;

    @Operation(summary = "집안일 자동 배정 요청", description = "AI 알고리즘(FastAPI)을 호출하여 지정된 기간 동안의 집안일을 배정하고 결과를 저장합니다.")
    @PostMapping("/auto-assign")
    public ResponseEntity<ApiResponse<String>> autoAssignChores(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        // 토큰에서 요청자 ID 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        // 시작일과 종료일 사이의 기간(일수) 계산 (종료일 포함이므로 +1)
        int periodDays = (int) ChronoUnit.DAYS.between(startDate, endDate) + 1;

        if (periodDays < 1) {
            throw new IllegalArgumentException("종료일은 시작일보다 같거나 이후여야 합니다.");
        }

        // 서비스 호출 (FastAPI 통신 -> DB 저장)
        assignmentService.assignChores(userId, startDate, periodDays);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "집안일 배정이 완료되었습니다.", "Success")
        );
    }
}