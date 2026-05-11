package com.partition.global.exception;

import com.partition.domain.common.dto.response.ApiResponse;
import com.partition.domain.user.exception.UserErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 우리가 정의한 CustomException 처리
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        BaseErrorCode code = ex.getErrorCode();

        log.warn("CustomException: code={} message={}", code.name(), code.getMessage());

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.onFailure(code.name(), code.getMessage(), null));
    }

    // 2. @Valid 검증 실패 처리 (RequestBody 필드 에러)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        // [수정] NPE 방지 로직 적용
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String errorMessage = (fieldError != null && fieldError.getDefaultMessage() != null)
                ? fieldError.getDefaultMessage()
                : "유효성 검증에 실패했습니다."; // 기본 메시지

        log.warn("Validation Failed: {}", errorMessage);

        // [수정] 하드코딩된 문자열("USER_2605") 대신 UserErrorCode 사용
        return ResponseEntity
                .status(400)
                .body(ApiResponse.onFailure(
                        UserErrorCode.MISSING_REQUIRED_VALUE.name(), // "MISSING_REQUIRED_VALUE"
                        errorMessage, // 구체적인 검증 실패 사유 (예: "이메일 형식이 아닙니다")
                        null
                ));
    }

    // 3. 존재하지 않는 URL 요청 (404)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleNoResourceFoundException(NoResourceFoundException ex) {
        return ResponseEntity
                .status(404)
                .body(ApiResponse.onFailure("404", "요청한 리소스를 찾을 수 없습니다.", null));
    }

    // 4. 그 외 예상치 못한 Exception (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex, HttpServletRequest request) {
        log.error("[{} {}] Internal Server Error: ", request.getMethod(), request.getRequestURI(), ex);

        return ResponseEntity
                .status(500)
                .body(ApiResponse.onFailure(
                        GlobalErrorCode.INTERNAL_SERVER_ERROR.name(),
                        "서버 내부 오류입니다. 관리자에게 문의하세요.",
                        null
                ));
    }
}