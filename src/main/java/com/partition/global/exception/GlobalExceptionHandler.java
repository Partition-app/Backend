package com.partition.global.exception;

import com.partition.domain.common.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        BaseErrorCode code = ex.getErrorCode();

        log.warn("CustomException: name={}, message={}", code.name(), code.getMessage());

        return ResponseEntity
                .status(code.getStatus())
                .body(ApiResponse.onFailure(code.name(), code.getMessage(), null));
    }

    // @Valid 검증 실패 처리 (RequestBody 필드 에러)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();

        log.warn("Validation Failed: {}", errorMessage);

        return ResponseEntity
                .status(400)
                .body(ApiResponse.onFailure("USER_2605", errorMessage, null));
    }

    // 그 외 예상치 못한 Exception (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception ex) {
        log.error("Internal Server Error: ", ex);

        return ResponseEntity
                .status(500)
                .body(ApiResponse.onFailure(
                        "SERVER_5001",
                        "서버 내부 오류입니다. 관리자에게 문의하세요.",
                        null
                ));
    }
}