package com.partition.domain.utilitybill.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.utilitybill.dto.request.CreateBillRequest;
import com.partition.domain.utilitybill.dto.response.BillResponse;
import com.partition.domain.utilitybill.dto.response.CreateBillResponse;
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
}