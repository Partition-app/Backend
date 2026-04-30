package com.partition.domain.reservation.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode implements BaseErrorCode {

    RESERVATION_1001(400, "예약 대상 이름을 입력해주세요."),
    RESERVATION_1002(409, "이미 존재하는 예약 대상입니다.");

    private final int status;
    private final String message;
}
