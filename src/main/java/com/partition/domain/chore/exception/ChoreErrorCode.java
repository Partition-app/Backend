package com.partition.domain.chore.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ChoreErrorCode implements BaseErrorCode {
    CHORE_1001(404, "존재하지 않는 집안일입니다."),
    CHORE_1002(403, "본인에게 배정된 집안일만 완료 처리할 수 있습니다."),
    CHORE_1003(409, "이미 완료 처리된 집안일입니다."),
    CHORE_4001(400, "같은 그룹의 멤버에게만 집안일을 배정할 수 있습니다."),
    CHORE_4002(403, "해당 집안일에 대한 권한이 없습니다."),
    ASSIGNMENT_API_ERROR(502, "집안일 배정 AI 응답 오류입니다."),
    HOUSEHOLD_ID_MISMATCH(401, "집 ID가 일치하지 않습니다."),
    ASSIGNMENT_API_UNAVAILABLE(503, "집안일 배정 AI 서버를 호출할 수 없습니다.");

    private final int status;
    private final String message;
}