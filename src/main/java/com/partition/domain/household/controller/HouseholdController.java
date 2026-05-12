package com.partition.domain.household.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.household.dto.request.CreateHouseholdRequest;
import com.partition.domain.household.dto.request.DelegateLeaderRequest;
import com.partition.domain.household.dto.request.JoinHouseholdRequest;
import com.partition.domain.household.dto.request.UpdateHouseholdNameRequest;
import com.partition.domain.household.dto.response.HouseholdInfoResponse;
import com.partition.domain.household.dto.response.HouseholdMemberResponse;
import com.partition.domain.household.service.HouseholdService;
import com.partition.entity.Household;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @Operation(summary = "그룹 이름 변경", description = "방장이 그룹 이름을 변경합니다.")
    @PatchMapping("/name")
    public ResponseEntity<ApiResponse<Void>> updateHouseholdName(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid UpdateHouseholdNameRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        householdService.updateHouseholdName(userId, request.getName());

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "그룹 이름 변경 성공", null));
    }

    @Operation(summary = "현재 그룹 정보 조회", description = "현재 소속된 그룹의 이름, ID, 방장 여부를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<HouseholdInfoResponse>> getHouseholdInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        HouseholdInfoResponse response = householdService.getHouseholdInfo(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "그룹 정보 조회 성공", response));
    }

    @Operation(summary = "그룹 멤버 조회", description = "현재 소속된 그룹의 멤버 목록을 조회합니다.")
    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<HouseholdMemberResponse>>> getMembers(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        List<HouseholdMemberResponse> members = householdService.getMembers(userId)
                .stream()
                .map(HouseholdMemberResponse::from)
                .toList();

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "그룹 멤버 조회 성공", members));
    }

    @Operation(summary = "리더 위임", description = "방장이 같은 그룹의 다른 멤버에게 방장 권한을 위임합니다.")
    @PatchMapping("/leader")
    public ResponseEntity<ApiResponse<Void>> delegateLeader(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid DelegateLeaderRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        householdService.delegateLeader(userId, request.getTargetUserId());

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "리더 위임 성공", null));
    }

    @Operation(summary = "그룹 나가기", description = "현재 소속된 그룹에서 나갑니다. 방장은 나갈 수 없습니다.")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> leaveHousehold(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        householdService.leaveHousehold(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "그룹 나가기 성공", null));
    }
}