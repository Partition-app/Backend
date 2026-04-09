package com.partition.domain.supply.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.supply.dto.request.CreateSettlementRequest;
import com.partition.domain.supply.dto.response.CreateSettlementResponse;
import com.partition.domain.supply.dto.response.SettlementListResponse;
import com.partition.domain.supply.service.SettlementService;
import com.partition.domain.supply.service.SupplyPurchaseService;
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
    private final SupplyPurchaseService supplyPurchaseService;

    @Operation(summary = "정산 대상 구매 목록 조회", description = "기간 내 정산되지 않은 구매 물품 목록과 인당 금액을 조회합니다.")
    @GetMapping("/purchases")
    public ResponseEntity<ApiResponse<SettlementListResponse>> getSettlementPurchases(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        SettlementListResponse result = supplyPurchaseService.getSettlementPurchases(userId, startDate, endDate);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "정산 대상 목록 조회 성공", result)
        );
    }

    @Operation(summary = "공용 구매 물품 정산 처리", description = "선택한 구매 기록을 하우스 멤버 수로 N/1 정산 처리합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSettlementResponse>> createSettlement(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateSettlementRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CreateSettlementResponse result = settlementService.createSettlement(userId, request);

        return ResponseEntity.status(201)
                .body(ApiResponse.onSuccess("201", "정산 메세지 발송 성공", result));
    }
}
