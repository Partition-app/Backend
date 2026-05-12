package com.partition.domain.alarm.controller;

import com.partition.domain.alarm.dto.response.AlarmListResponse;
import com.partition.domain.alarm.dto.response.ReadAlarmResponse;
import com.partition.domain.alarm.service.AlarmService;
import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;

    @Operation(summary = "알림 목록 조회", description = "내 알림 전체를 최신순으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<AlarmListResponse>> getAlarms(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        AlarmListResponse result = alarmService.getAlarms(userId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "알림 목록 조회 성공", result)
        );
    }

    @Operation(summary = "알림 읽음 처리", description = "특정 알림을 읽음 처리합니다.")
    @PatchMapping("/{alarmId}/read")
    public ResponseEntity<ApiResponse<ReadAlarmResponse>> readAlarm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long alarmId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ReadAlarmResponse result = alarmService.readAlarm(userId, alarmId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "알림 읽음 처리 성공", result)
        );
    }

    @Operation(summary = "알림 삭제", description = "특정 알림을 삭제합니다.")
    @DeleteMapping("/{alarmId}")
    public ResponseEntity<ApiResponse<Void>> deleteAlarm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long alarmId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        alarmService.deleteAlarm(userId, alarmId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "알림 삭제 성공", null));
    }
}
