package com.partition.domain.household.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.household.dto.request.CreateHouseholdRequest;
import com.partition.domain.household.dto.request.JoinHouseholdRequest;
import com.partition.domain.household.service.HouseholdService;
import com.partition.entity.Household;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor
public class HouseholdController {

    private final HouseholdService householdService;

    @Operation(summary = "새로운 그룹 만들기", description = "그룹을 생성하고 유저를 방장(LEADER)으로 설정합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createHousehold(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateHouseholdRequest request) {

        // 토큰에서 유저 ID 추출
        Long userId = Long.parseLong(userDetails.getUsername());

        // 서비스 호출
        Household household = householdService.createHousehold(userId, request.getName());

        // 결과 반환 (초대코드 및 그룹 ID 포함)
        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        "200",
                        "그룹 생성 성공",
                        Map.of(
                                "householdId", household.getId(),
                                "inviteCode", household.getInviteCode()
                        )
                )
        );
    }


    // 기존 그룹 참여 API
    @Operation(summary = "그룹 참여하기", description = "초대 코드를 입력하여 기존 그룹에 참여합니다.")
    @PostMapping("/join")
    public ResponseEntity<ApiResponse<Map<String, Object>>> joinHousehold(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid JoinHouseholdRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());

        // 서비스 호출
        Household household = householdService.joinHousehold(userId, request.getInviteCode());

        return ResponseEntity.ok(
                ApiResponse.onSuccess(
                        "200",
                        "그룹 참여 성공",
                        Map.of(
                                "householdId", household.getId(),
                                "householdName", household.getName()
                        )
                )
        );
    }
}