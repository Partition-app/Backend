package com.partition.domain.alarm.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AlarmErrorCode implements BaseErrorCode {
    ALARM_1001(404, "존재하지 않는 알림입니다."),
    ALARM_1002(403, "해당 알림에 대한 권한이 없습니다.");

    private final int status;
    private final String message;
}