package com.partition.domain.schedule.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ScheduleRequest {

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @NotNull(message = "날짜는 필수입니다.")
    private LocalDate date;
}