package com.partition.domain.schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class ScheduleUpdateRequest {
    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private LocalDate date;   // 변경 없으면 null 가능
    private LocalTime time;   // 시간을 지우려면 null로 보내면 됨
}