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
    RESERVATION_2005(409, "해당 시간에 이미 예약이 존재합니다."),
    RESERVATION_2006(400, "조회 시작 날짜를 입력해주세요."),
    RESERVATION_2007(400, "조회 종료 날짜를 입력해주세요."),
    RESERVATION_2008(404, "존재하지 않는 예약입니다."),
    RESERVATION_2009(403, "본인의 예약만 수정할 수 있습니다."),
    RESERVATION_2010(400, "삭제할 예약을 선택해주세요."),
    RESERVATION_2011(400, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)");

    private final int status;
    private final String message;
}
