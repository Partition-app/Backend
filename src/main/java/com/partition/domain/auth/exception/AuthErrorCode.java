package com.partition.domain.auth.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode { // 인터페이스 구현

    // 21xx: Auth 관련
    EMPTY_ACCESS_TOKEN(400, "액세스 토큰이 없습니다."),
    INVALID_ACCESS_TOKEN(401, "유효하지 않은 액세스 토큰입니다."),
    EXPIRED_ACCESS_TOKEN(401, "액세스 토큰이 만료되었습니다."),
    EMPTY_REFRESH_TOKEN(400, "리프레시 토큰이 없습니다."),
    INVALID_REFRESH_TOKEN(401, "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(401, "리프레시 토큰이 만료되었습니다."),
    KAKAO_LOGIN_FAILED(502, "카카오 로그인에 실패했습니다."),
    DEACTIVATED_USER(403, "탈퇴한 계정입니다.");

    private final int status;
    private final String message;
}