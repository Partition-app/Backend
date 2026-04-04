package com.partition.domain.supply.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.supply.dto.request.CreateSupplyPurchaseRequest;
import com.partition.domain.supply.dto.response.CreateSupplyPurchaseResponse;
import com.partition.domain.supply.dto.response.SupplyPurchaseListResponse;
import com.partition.domain.supply.service.SupplyPurchaseService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/supplies/purchases")
@RequiredArgsConstructor
public class SupplyPurchaseController {

    private final SupplyPurchaseService supplyPurchaseService;

    @Operation(summary = "공용 소비 물품 구매 기록 등록", description = "공동 구매 물품을 직접 입력해서 수동으로 추가합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateSupplyPurchaseResponse>> createPurchase(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateSupplyPurchaseRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CreateSupplyPurchaseResponse result = supplyPurchaseService.createPurchase(userId, request);

        return ResponseEntity.status(201)
                .body(ApiResponse.onSuccess("201", "공용 소비 물품 구매 기록 등록 성공", result));
    }

    @Operation(summary = "공용 소비 물품 구매 기록 조회", description = "기간 내 공용 소비 물품 구매 기록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<SupplyPurchaseListResponse>> getPurchases(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        SupplyPurchaseListResponse result = supplyPurchaseService.getPurchases(userId, startDate, endDate);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "공용 소비 물품 구매 기록 조회 성공", result)
        );
    }
}
