package com.partition.domain.calender.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalendarMonthlyResponse {
    private LocalDate date;
    private long choreCount;
    private long scheduleCount;
    private long utilityBillsCount;
}