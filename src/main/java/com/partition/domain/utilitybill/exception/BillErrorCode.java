package com.partition.domain.utilitybill.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BillErrorCode implements BaseErrorCode {
    BILL_1001(400, "공과금 종류는 필수입니다."),
    BILL_1002(400, "존재하지 않는 공과금 종류입니다."),
    BILL_1003(400, "날짜는 필수입니다."),
    BILL_1004(400, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),
    BILL_1005(400, "금액은 필수입니다."),
    BILL_1006(400, "금액은 0원 이상이어야 합니다."),
    BILL_2001(400, "시작일은 필수입니다."),
    BILL_2002(400, "종료일은 필수입니다."),
    BILL_2003(400, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),
    BILL_2004(400, "시작일은 종료일보다 이전이어야 합니다."),
    BILL_2005(400, "하우스 멤버가 아닌 유저입니다."),
    BILL_9001(404, "사용자를 찾을 수 없습니다."),
    BILL_9002(404, "가구 정보를 찾을 수 없습니다.");

    private final int status;
    private final String message;
}