package com.partition.domain.calender.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarDailyResponse {
    private String category;     // "CHORE", "SCHEDULE", "UTILITY_BILL"
    private Long id;
    private String title;
    private String assigneeName;
    private Boolean isCompleted;
    private Boolean isOwner;
    private Integer amount;      // 공과금에만 사용
}