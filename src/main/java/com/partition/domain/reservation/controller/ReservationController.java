package com.partition.domain.reservation.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.reservation.dto.request.CreateReservationItemRequest;
import com.partition.domain.reservation.dto.request.DeleteReservationItemRequest;
import com.partition.domain.reservation.dto.request.UpdateReservationItemRequest;
import com.partition.domain.reservation.dto.response.CreateReservationItemResponse;
import com.partition.domain.reservation.dto.response.ReservationItemResponse;
import com.partition.domain.reservation.dto.response.UpdateReservationItemResponse;
import com.partition.domain.reservation.dto.request.CreateReservationRequest;
import com.partition.domain.reservation.dto.response.CreateReservationResponse;
import com.partition.domain.reservation.service.ReservationItemService;
import com.partition.domain.reservation.service.ReservationService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationItemService reservationItemService;
    private final ReservationService reservationService;

    @Operation(summary = "예약 대상 추가", description = "하우스에 새로운 예약 대상을 추가합니다.")
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CreateReservationItemResponse>> createReservationItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateReservationItemRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());

        CreateReservationItemResponse result = reservationItemService.createItem(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess("201", "예약 대상 추가 성공", result));
    }

    @Operation(summary = "예약 대상 목록 조회", description = "하우스의 예약 대상 목록을 조회합니다.")
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<ReservationItemResponse>>> getReservationItems(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());

        List<ReservationItemResponse> result = reservationItemService.getItems(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "예약 대상 목록 조회 성공", result));
    }

    @Operation(summary = "예약 대상 수정", description = "예약 대상 이름을 수정합니다.")
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<UpdateReservationItemResponse>> updateReservationItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long itemId,
            @RequestBody UpdateReservationItemRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());

        UpdateReservationItemResponse result = reservationItemService.updateItem(userId, itemId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "예약 대상 수정 성공", result));
    }

    @Operation(summary = "예약 대상 삭제", description = "예약 대상을 삭제합니다.")
    @DeleteMapping("/items")
    public ResponseEntity<ApiResponse<?>> deleteReservationItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody DeleteReservationItemRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());

        reservationItemService.deleteItems(userId, request);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "예약 대상 삭제 성공"));
    }

    @Operation(summary = "예약하기", description = "예약 대상에 대한 시간 예약을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateReservationResponse>> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateReservationRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());

        CreateReservationResponse result = reservationService.createReservation(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.onSuccess("201", "예약 성공", result));
    }
}
