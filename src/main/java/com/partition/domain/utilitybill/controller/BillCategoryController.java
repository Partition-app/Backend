package com.partition.domain.utilitybill.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.utilitybill.dto.response.BillCategoryResponse;
import com.partition.domain.utilitybill.service.BillCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillCategoryController {

    private final BillCategoryService billCategoryService;

    @Operation(summary = "공과금 카테고리 조회", description = "공과금 카테고리 목록을 조회합니다.")
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<BillCategoryResponse>>> getCategories() {
        List<BillCategoryResponse> result = billCategoryService.getCategories();
        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "공과금 카테고리 조회 성공", result)
        );
    }
}