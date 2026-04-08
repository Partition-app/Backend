package com.partition.domain.supply.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.supply.dto.request.CreateSupplyPurchaseRequest;
import com.partition.domain.supply.dto.request.UpdateSupplyPurchaseRequest;
import com.partition.domain.supply.dto.response.CreateSupplyPurchaseResponse;
import com.partition.domain.supply.dto.response.ReceiptAnalysisResponse;

import com.partition.domain.supply.dto.response.SupplyPurchaseListResponse;
import com.partition.domain.supply.dto.response.UpdateSupplyPurchaseResponse;
import com.partition.domain.supply.service.ReceiptAnalysisService;
import com.partition.domain.supply.service.SupplyPurchaseService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/supplies/purchases")
@RequiredArgsConstructor
public class SupplyPurchaseController {

    private final SupplyPurchaseService supplyPurchaseService;
    private final ReceiptAnalysisService receiptAnalysisService;

    @Operation(summary = "영수증 이미지 분석", description = "영수증 이미지를 업로드하면 CV 모델이 분석하여 구매 물품 정보를 반환합니다.")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<ReceiptAnalysisResponse>> analyzeReceiptImage(
            @RequestPart("image") MultipartFile image
    ) {
        ReceiptAnalysisResponse result = receiptAnalysisService.analyzeReceipt(image);
        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "영수증 이미지 분석 성공", result)
        );
    }

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

    @Operation(summary = "공용 소비 물품 구매 기록 수정", description = "변경할 필드만 포함하여 구매 기록을 수정합니다.")
    @PatchMapping("/{purchaseId}")
    public ResponseEntity<ApiResponse<UpdateSupplyPurchaseResponse>> updatePurchase(
            @PathVariable Long purchaseId,
            @RequestBody UpdateSupplyPurchaseRequest request
    ) {
        UpdateSupplyPurchaseResponse result = supplyPurchaseService.updatePurchase(purchaseId, request);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "공용 소비 물품 구매 기록 수정 성공", result)
        );
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
