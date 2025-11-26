package com.partition.domain.schedule.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ScheduleErrorCode implements BaseErrorCode {

    SCHEDULE_NOT_FOUND(404, "해당 일정을 찾을 수 없습니다."),
    NO_PERMISSION_TO_MODIFY(403, "일정을 수정할 권한이 없습니다."),
    NO_PERMISSION_TO_DELETE(403, "일정을 삭제할 권한이 없습니다.");

    private final int status;
    private final String message;
}