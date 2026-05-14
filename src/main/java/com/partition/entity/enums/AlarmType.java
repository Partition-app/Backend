package com.partition.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AlarmType {
    SUPPLY_SETTLEMENT_REQUESTED("공동 구매 정산이 요청되었습니다."),
    SUPPLY_SETTLEMENT_CONFIRMED("공동 구매 정산이 완료되었습니다."),
    BILL_SETTLEMENT_REQUESTED("공과금 정산이 요청되었습니다."),
    BILL_SETTLEMENT_CONFIRMED("공과금 정산이 완료되었습니다."),
    BILL_PAYMENT_REMINDER("이번 달 공과금 금액을 입력해주세요.");

    private final String message;
}