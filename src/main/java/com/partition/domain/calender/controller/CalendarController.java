package com.partition.domain.calender.controller;

import com.partition.domain.calender.dto.response.CalendarMonthlyResponse;
import com.partition.domain.calender.service.CalendarService;
import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.global.config.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calendars")
@RequiredArgsConstructor
public class CalendarController {

    private final CalendarService calendarService;

    @Operation(summary = "월간 캘린더 조회", description = "특정 월의 날짜별 일정(일정, 집안일, 공과금) 개수를 반환합니다.")
    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<CalendarMonthlyResponse>>> getMonthlyCalendar(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        Long userId = Long.parseLong(userDetails.getUsername());
        List<CalendarMonthlyResponse> result = calendarService.getMonthlyCalendar(userId, year, month);

        return ResponseEntity.ok(
                ApiResponse.onSuccess("200", "월간 캘린더 조회 성공", result)
        );
    }
}