package com.partition.domain.schedule.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements BaseErrorCode {

    SCHEDULE_NOT_FOUND(404, "해당 일정을 찾을 수 없습니다."),
    NO_PERMISSION(403, "해당 일정에 대한 권한이 없습니다.");


    private final int status;
    private final String message;
}