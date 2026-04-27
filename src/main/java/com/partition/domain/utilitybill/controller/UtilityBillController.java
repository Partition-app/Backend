package com.partition.domain.utilitybill.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.utilitybill.dto.request.CreateBillRequest;
import com.partition.domain.utilitybill.dto.request.CreateBillSettlementRequest;
import com.partition.domain.utilitybill.dto.response.BillResponse;
import com.partition.domain.utilitybill.dto.response.BillSettlementDetailResponse;
import com.partition.domain.utilitybill.dto.response.BillSettlementListResponse;
import com.partition.domain.utilitybill.dto.response.BillSettlementRequestedListResponse;
import com.partition.domain.utilitybill.dto.response.ConfirmBillSettlementResponse;
import com.partition.domain.utilitybill.dto.response.CreateBillResponse;
import com.partition.domain.utilitybill.dto.response.CreateBillSettlementResponse;
import com.partition.domain.utilitybill.service.BillSettlementService;
import com.partition.domain.utilitybill.service.UtilityBillService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class UtilityBillController {

    private final UtilityBillService utilityBillService;
    private final BillSettlementService billSettlementService;

    @Operation(summary = "공과금 목록 조회", description = "기간별 공과금 내역을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BillResponse>>> getBills(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<BillResponse> result = utilityBillService.getBills(userId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "공과금 목록 조회 성공", result));
    }

    @Operation(summary = "공과금 수동 추가", description = "공과금 내역을 수동으로 등록합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateBillResponse>> createBill(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateBillRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CreateBillResponse result = utilityBillService.createBill(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess("201", "공과금 등록 성공", result));
    }

    @Operation(summary = "정산 대상 공과금 목록 조회", description = "기간 내 미정산 공과금 목록과 인당 금액을 조회합니다.")
    @GetMapping("/settlement/list")
    public ResponseEntity<ApiResponse<BillSettlementListResponse>> getSettlementBills(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        BillSettlementListResponse result = utilityBillService.getSettlementBills(userId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "정산 대상 공과금 목록 조회 성공", result));
    }

    @Operation(summary = "공과금 정산 요청", description = "선택한 공과금을 하우스 멤버 수로 1/N 정산 요청합니다.")
    @PostMapping("/settlement")
    public ResponseEntity<ApiResponse<CreateBillSettlementResponse>> createSettlement(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateBillSettlementRequest request
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        CreateBillSettlementResponse result = billSettlementService.createSettlement(userId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess("201", "공과금 정산 메세지 발송 성공", result));
    }

    @Operation(summary = "공과금 정산 완료 처리", description = "정산 요청된 공과금 내역을 일괄 정산 완료 처리합니다.")
    @PatchMapping("/settlement/{settlementId}/confirm")
    public ResponseEntity<ApiResponse<ConfirmBillSettlementResponse>> confirmSettlement(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long settlementId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        ConfirmBillSettlementResponse result = billSettlementService.confirmSettlement(userId, settlementId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "공과금 정산 완료 처리 성공", result));
    }

    @Operation(summary = "정산 요청된 공과금 목록 조회", description = "정산 요청 상태인 공과금 정산 목록을 조회합니다.")
    @GetMapping("/settlement/requested")
    public ResponseEntity<ApiResponse<BillSettlementRequestedListResponse>> getRequestedSettlements(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        BillSettlementRequestedListResponse result = billSettlementService.getRequestedSettlements(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "정산 요청 목록 조회 성공", result));
    }

    @Operation(summary = "공과금 정산 상세 조회", description = "정산 ID로 공과금 정산 상세 정보를 조회합니다.")
    @GetMapping("/settlement/{settlementId}")
    public ResponseEntity<ApiResponse<BillSettlementDetailResponse>> getSettlementDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long settlementId
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        BillSettlementDetailResponse result = billSettlementService.getSettlementDetail(userId, settlementId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "공과금 정산 상세 조회 성공", result));
    }
}