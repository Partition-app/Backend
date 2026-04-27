package com.partition.domain.supply.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.supply.dto.request.CreateSettlementRequest;
import com.partition.domain.supply.dto.response.ConfirmSettlementResponse;
import com.partition.domain.supply.dto.response.CreateSettlementResponse;
import com.partition.domain.supply.dto.response.SettlementListResponse;
import com.partition.domain.supply.dto.response.SettlementRequestedListResponse;
import com.partition.domain.supply.dto.response.SupplySettlementDetailResponse;
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

    @Operation(summary = "공동 구매 물품 정산 처리", description = "선택한 구매 기록을 하우스 멤버 수로 1/N 정산 처리합니다.")
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

    @Operation(summary = "공동 구매 물품 정산 완료 처리", description = "정산 요청된 내역을 일괄 정산 완료 처리합니다.")
    @PatchMapping("/{settlementId}/confirm")
    public ResponseEntity<ApiResponse<ConfirmSettlementResponse>> confirmSettlement(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long settlementId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ConfirmSettlementResponse result = settlementService.confirmSettlement(userId, settlementId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "정산 완료 처리 성공", result)
        );
    }

    @Operation(summary = "정산 요청된 공동 구매 물품 목록 조회", description = "정산 요청 상태인 공동 구매 정산 목록을 조회합니다.")
    @GetMapping("/requested")
    public ResponseEntity<ApiResponse<SettlementRequestedListResponse>> getRequestedSettlements(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        SettlementRequestedListResponse result = settlementService.getRequestedSettlements(userId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "정산 요청 목록 조회 성공", result)
        );
    }

    @Operation(summary = "공동 구매 물품 정산 상세 조회", description = "정산 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{settlementId}")
    public ResponseEntity<ApiResponse<SupplySettlementDetailResponse>> getSettlementDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long settlementId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        SupplySettlementDetailResponse result = settlementService.getSettlementDetail(userId, settlementId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "정산 상세 조회 성공", result)
        );
    }
}
