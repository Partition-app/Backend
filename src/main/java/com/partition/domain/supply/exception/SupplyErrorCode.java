package com.partition.domain.supply.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SupplyErrorCode implements BaseErrorCode {
    SUPPLY_1005(404, "등록된 카테고리가 없습니다."),
    SUPPLY_2001(400, "시작일은 필수입니다."),
    SUPPLY_2002(400, "종료일은 필수입니다."),
    SUPPLY_2003(400, "종료일은 시작일 이후여야 합니다."),
    SUPPLY_2004(400, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),
    SUPPLY_3001(400, "물품명은 필수입니다."),
    SUPPLY_3002(400, "구매 날짜는 필수입니다."),
    SUPPLY_3003(400, "날짜 형식이 올바르지 않습니다. (yyyy-MM-dd)"),
    SUPPLY_3004(400, "금액은 0원 이상이어야 합니다."),
    SUPPLY_3005(400, "수량은 1개 이상이어야 합니다."),
    SUPPLY_3006(400, "존재하지 않는 카테고리입니다."),
    SUPPLY_3007(404, "사용자를 찾을 수 없습니다."),
    SUPPLY_3008(404, "가구 정보를 찾을 수 없습니다.");

    private final int status;
    private final String message;
}
