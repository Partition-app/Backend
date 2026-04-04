package com.partition.domain.supply.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.supply.dto.response.SupplyCategoryResponse;
import com.partition.domain.supply.service.SupplyCategoryService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/supplies/categories")
@RequiredArgsConstructor
public class SupplyCategoryController {

    private final SupplyCategoryService supplyCategoryService;

    @Operation(summary = "공용 소비 물품 카테고리 조회", description = "현재 household에 등록된 공용 소비 물품 카테고리를 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SupplyCategoryResponse>>> getCategories(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        Long userId = Long.parseLong(userDetails.getUsername());
        List<SupplyCategoryResponse> result = supplyCategoryService.getCategories(userId);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "공용 소비 물품 카테고리 조회 성공", result)
        );
    }
}
