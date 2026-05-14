package com.partition.domain.homeshare.controller;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.homeshare.dto.request.HomeLocationRequest;
import com.partition.domain.homeshare.dto.request.LocationConsentRequest;
import com.partition.domain.homeshare.dto.request.NearHomeEventRequest;
import com.partition.domain.homeshare.dto.response.HomeLocationResponse;
import com.partition.domain.homeshare.dto.response.LocationConsentResponse;
import com.partition.domain.homeshare.dto.response.NearHomeEventResponse;
import com.partition.domain.homeshare.service.HomeShareService;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/households")
@RequiredArgsConstructor
public class HomeShareController {

    private final HomeShareService homeShareService;

    @Operation(summary = "위치 공유 동의 조회", description = "현재 위치 공유 동의 여부를 조회합니다.")
    @GetMapping("/location-consent")
    public ResponseEntity<ApiResponse<LocationConsentResponse>> getConsent(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        LocationConsentResponse result = homeShareService.getConsent(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "위치 공유 동의 조회 성공", result));
    }

    @Operation(summary = "위치 공유 동의 저장", description = "귀가 공유 토글 ON/OFF 시 위치 공유 동의 여부를 저장합니다.")
    @PostMapping("/location-consent")
    public ResponseEntity<ApiResponse<LocationConsentResponse>> saveConsent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody LocationConsentRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        LocationConsentResponse result = homeShareService.saveConsent(userId, request.isAgreed());

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "위치 공유 동의가 저장되었습니다.", result));
    }

    @Operation(summary = "집 위치 조회", description = "가구의 현재 등록된 집 위치를 조회합니다.")
    @GetMapping("/home-location")
    public ResponseEntity<ApiResponse<HomeLocationResponse>> getHomeLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = Long.parseLong(userDetails.getUsername());
        HomeLocationResponse result = homeShareService.getHomeLocation(userId);

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "집 위치 조회 성공", result));
    }

    @Operation(summary = "집 위치 저장", description = "가구의 집 위치(위도/경도/반경)를 저장합니다. 이미 등록된 경우 덮어씁니다.")
    @PostMapping("/home-location")
    public ResponseEntity<ApiResponse<HomeLocationResponse>> saveHomeLocation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody HomeLocationRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        HomeLocationResponse result = homeShareService.saveHomeLocation(
                userId, request.getLat(), request.getLng(), request.getRadius());

        return ResponseEntity.ok(ApiResponse.onSuccess("200", "집 위치가 저장되었습니다.", result));
    }

    @Operation(summary = "집 근처 진입 이벤트 전송", description = "집 반경 진입 시 룸메이트에게 FCM 푸시 알림을 보냅니다. 30분 쿨다운 적용.")
    @PostMapping("/location-events/near-home")
    public ResponseEntity<ApiResponse<NearHomeEventResponse>> handleNearHomeEvent(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody NearHomeEventRequest request) {

        Long userId = Long.parseLong(userDetails.getUsername());
        NearHomeEventResponse result = homeShareService.handleNearHomeEvent(userId, request.getEventType());

        String message = result.getNotifiedUserCount() > 0
                ? "룸메이트에게 집 근처 알림을 보냈습니다."
                : "최근에 이미 알림을 보냈습니다.";

        return ResponseEntity.ok(ApiResponse.onSuccess("200", message, result));
    }
}
