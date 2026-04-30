package com.partition.domain.reservation.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReservationErrorCode implements BaseErrorCode {

    RESERVATION_1001(400, "예약 대상 이름을 입력해주세요."),
    RESERVATION_1002(409, "이미 존재하는 예약 대상입니다."),
    RESERVATION_1003(404, "존재하지 않는 예약 대상입니다."),
    RESERVATION_1004(400, "삭제할 예약 대상을 선택해주세요."),

    RESERVATION_2001(400, "예약 대상을 선택해주세요."),
    RESERVATION_2002(400, "예약 시작 시간을 입력해주세요."),
    RESERVATION_2003(400, "예약 종료 시간을 입력해주세요."),
    RESERVATION_2004(400, "종료 시간은 시작 시간 이후여야 합니다."),
    RESERVATION_2005(409, "해당 시간에 이미 예약이 존재합니다.");

    private final int status;
    private final String message;
}
