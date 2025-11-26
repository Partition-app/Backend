package com.partition.domain.schedule.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class ScheduleUpdateRequest {
    // 수정할 때 값이 안 넘어오면(null이면) 기존 값을 유지하기 위해 Validation 어노테이션을 뺐습니다.
    private String content;
    private LocalDate date;
}