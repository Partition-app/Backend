package com.partition.domain.chore.controller;

import com.partition.domain.chore.dto.request.AutoAssignRequest;
import com.partition.domain.chore.dto.request.CreateChoreRequest;
import com.partition.domain.chore.dto.request.UpdateChoreRequest;
import com.partition.domain.chore.dto.response.AssignmentResponse;
import com.partition.domain.chore.dto.response.ChoreResponse;
import com.partition.domain.chore.dto.response.CompleteChoreResponse;
import com.partition.domain.chore.service.ChoreAssignmentService;
import com.partition.domain.chore.service.ChoreService;
import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/chores")
@RequiredArgsConstructor
public class ChoreController {

    private final ChoreAssignmentService assignmentService;
    private final ChoreService choreService;

    @Operation(summary = "집안일 수동 등록", description = "같은 그룹의 멤버에게 집안일을 수동으로 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<ChoreResponse>> createChore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateChoreRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        ChoreResponse result = choreService.createChore(userId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "집안일 등록 성공", result));
    }

    @Operation(summary = "집안일 자동 배정 요청", description = "AI 알고리즘(FastAPI)을 호출하여 지정된 기간 동안, 선택된 집안일을 배정하고 결과를 저장합니다.")
    @PostMapping("/auto-assign")
    public ResponseEntity<ApiResponse<List<AssignmentResponse.AssignmentResult>>> autoAssignChores(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid AutoAssignRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());

        int periodDays = (int) ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;

        if (periodDays < 1) {
            throw new IllegalArgumentException("종료일은 시작일보다 같거나 이후여야 합니다.");
        }

        List<AssignmentResponse.AssignmentResult> result = assignmentService.assignChores(userId, request.getStartDate(), periodDays, request.getChoreTypes());

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "집안일 배정이 완료되었습니다.", result)
        );
    }

    @Operation(summary = "집안일 수정", description = "배정된 집안일의 담당자 또는 날짜를 수정합니다.")
    @PatchMapping("/{choreId}")
    public ResponseEntity<ApiResponse<ChoreResponse>> updateChore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long choreId,
            @RequestBody UpdateChoreRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        ChoreResponse result = choreService.updateChore(userId, choreId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "집안일 수정 성공", result));
    }

    @Operation(summary = "집안일 삭제", description = "배정된 집안일을 삭제합니다.")
    @DeleteMapping("/{choreId}")
    public ResponseEntity<ApiResponse<?>> deleteChore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long choreId) {

        Long userId = Long.parseLong(userDetails.getUsername());
        choreService.deleteChore(userId, choreId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "집안일 삭제 성공"));
    }

    @Operation(summary = "집안일 완료 처리", description = "본인에게 배정된 집안일을 완료 처리합니다.")
    @PatchMapping("/{choreId}/complete")
    public ResponseEntity<ApiResponse<CompleteChoreResponse>> completeChore(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long choreId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CompleteChoreResponse result = choreService.completeChore(userId, choreId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "집안일 완료 처리 성공", result)
        );
    }
}