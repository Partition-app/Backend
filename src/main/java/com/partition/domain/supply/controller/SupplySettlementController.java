package com.partition.domain.supply.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.supply.dto.request.CreateSettlementRequest;
import com.partition.domain.supply.dto.response.CreateSettlementResponse;
import com.partition.domain.supply.service.SettlementService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplies/settlement")
@RequiredArgsConstructor
public class SupplySettlementController {

    private final SettlementService settlementService;

    @Operation(summary = "공용 구매 물품 정산 처리", description = "선택한 구매 기록을 하우스 멤버 수로 N/1 정산 처리합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSettlementResponse>> createSettlement(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateSettlementRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CreateSettlementResponse result = settlementService.createSettlement(userId, request);

        return ResponseEntity.status(201)
                .body(ApiResponse.onSuccess("201", "정산 처리 성공", result));
    }
}
