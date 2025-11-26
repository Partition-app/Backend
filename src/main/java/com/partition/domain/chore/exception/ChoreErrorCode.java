package com.partition.domain.chore.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChoreErrorCode implements BaseErrorCode {
    ASSIGNMENT_API_ERROR(502, "집안일 배정 AI 응답 오류입니다."),
    HOUSEHOLD_ID_MISMATCH(401, "집 ID가 일치하지 않습니다."),
    ASSIGNMENT_API_UNAVAILABLE(503, "집안일 배정 AI 서버를 호출할 수 없습니다.");

    private final int status;
    private final String message;
}