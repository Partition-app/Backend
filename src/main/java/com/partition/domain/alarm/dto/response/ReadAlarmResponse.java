package com.partition.domain.alarm.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadAlarmResponse {
    private Long alarmId;
    private Boolean isRead;
}