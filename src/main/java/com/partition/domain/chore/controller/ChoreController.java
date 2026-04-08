package com.partition.domain.chore.controller;

import com.partition.domain.chore.dto.request.AutoAssignRequest;
import com.partition.domain.chore.dto.response.AssignmentResponse;
import com.partition.domain.chore.service.ChoreAssignmentService;
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
}