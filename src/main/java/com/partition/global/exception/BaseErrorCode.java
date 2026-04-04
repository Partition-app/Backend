package com.partition.global.exception;

public interface BaseErrorCode {
    int getStatus();      // HTTP 상태 코드 (400, 401, 404 등)
    String getMessage();  // 에러 메시지
    String name();        // Enum의 이름 (AUTH_2101 등) - Enum이 기본적으로 가지고 있음
}