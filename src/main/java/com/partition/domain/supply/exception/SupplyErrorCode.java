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
    SUPPLY_3008(404, "가구 정보를 찾을 수 없습니다."),
    SUPPLY_3009(404, "존재하지 않는 구매 기록입니다."),
    SUPPLY_3010(403, "해당 구매 기록에 대한 권한이 없습니다."),
    SUPPLY_3011(409, "정산된 구매 기록은 수정할 수 없습니다."),
    SUPPLY_5001(400, "정산할 구매 기록을 선택해주세요."),
    SUPPLY_5002(404, "존재하지 않는 구매 기록이 포함되어 있습니다."),
    SUPPLY_5003(409, "이미 정산된 구매 기록이 포함되어 있습니다."),
    SUPPLY_5004(400, "하우스 멤버가 아닌 유저가 포함되어 있습니다."),
    SUPPLY_5005(400, "정산 대상 멤버를 선택해주세요."),
    SUPPLY_4001(400, "이미지 파일은 필수입니다."),
    SUPPLY_4002(400, "지원하지 않는 파일 형식입니다. (jpg, jpeg, png)"),
    SUPPLY_4003(500, "이미지 분석에 실패했습니다. 다시 시도해주세요."),
    SUPPLY_4004(422, "영수증을 인식할 수 없습니다. 이미지를 확인해주세요.");

    private final int status;
    private final String message;
}
