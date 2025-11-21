package com.partition.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements BaseErrorCode {

    INTERNAL_SERVER_ERROR(500, "서버 내부 오류입니다."),
    INVALID_INPUT_VALUE(400, "입력값이 올바르지 않습니다.");

    private final int status;
    private final String message;
}