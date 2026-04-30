package com.partition.domain.report.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.report.dto.response.PartitionReportResponse;
import com.partition.domain.report.dto.response.SettlementReportResponse;
import com.partition.domain.report.service.ReportService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "파티션 리포트 조회", description = "기간별 집안일·공용소비물품·예약 리포트와 전월 대비 공과금 변동을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<PartitionReportResponse>> getReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Long userId = Long.parseLong(userDetails.getUsername());

        PartitionReportResponse result = reportService.getReport(userId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "파티션 리포트 조회 성공", result));
    }

    @Operation(summary = "정산 리포트 조회", description = "기간별 정산 완료된 공용물품·공과금 내역을 조회합니다.")
    @GetMapping("/settlement")
    public ResponseEntity<ApiResponse<SettlementReportResponse>> getSettlementReport(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Long userId = Long.parseLong(userDetails.getUsername());

        SettlementReportResponse result = reportService.getSettlementReport(userId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "정산 리포트 조회 성공", result));
    }
}
