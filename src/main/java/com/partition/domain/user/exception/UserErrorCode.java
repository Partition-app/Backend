package com.partition.domain.user.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    // 26xx: User 관련
    USER_NOT_FOUND(404, "존재하지 않는 회원입니다."),
    DUPLICATED_EMAIL(409, "이미 가입된 이메일입니다."),
    DUPLICATED_NICKNAME(409, "이미 사용중인 닉네임입니다."),
    MISSING_REQUIRED_VALUE(400, "필수 항목이 누락되었습니다.");

    private final int status;
    private final String message;
}