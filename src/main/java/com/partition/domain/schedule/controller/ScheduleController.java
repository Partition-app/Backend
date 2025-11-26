package com.partition.domain.schedule.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.schedule.dto.request.ScheduleRequest;
import com.partition.domain.schedule.dto.request.ScheduleUpdateRequest;
import com.partition.domain.schedule.service.ScheduleService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @Operation(summary = "일정 등록", description = "새로운 일정을 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Long>>> createSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ScheduleRequest request) {

        // 토큰에서 유저 ID 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        // 서비스 호출 (ID 반환)
        Long scheduleId = scheduleService.createSchedule(userId, request);

        // 결과 반환
        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "일정 등록 성공", Map.of("scheduleId", scheduleId))
        );
    }

    @Operation(summary = "일정 수정", description = "기존 일정을 수정합니다. 변경하고 싶은 필드만 보내세요.")
    @PatchMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Map<String, Long>>> updateSchedule(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long scheduleId,
            @RequestBody ScheduleUpdateRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        scheduleService.updateSchedule(userId, scheduleId, request);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "일정 수정 성공", Map.of("scheduleId", scheduleId))
        );
    }
}