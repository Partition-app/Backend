package com.partition.domain.report.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportErrorCode implements BaseErrorCode {

    REPORT_1001(400, "시작일은 필수입니다."),
    REPORT_1002(400, "종료일은 필수입니다."),
    REPORT_1003(400, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),
    REPORT_1004(400, "시작일은 종료일보다 이전이어야 합니다."),
    REPORT_1005(400, "하우스 멤버가 아닌 유저입니다.");

    private final int status;
    private final String message;
}
