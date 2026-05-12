package com.partition.domain.household.exception;

import com.partition.global.exception.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HouseholdErrorCode implements BaseErrorCode {

    HOUSEHOLD_NOT_FOUND(404, "해당 그룹을 찾을 수 없습니다."),
    INVALID_INVITE_CODE(400, "유효하지 않은 초대 코드입니다."),
    ALREADY_JOINED(409, "이미 그룹에 소속되어 있습니다."),
    INVITE_CODE_GENERATION_FAILED(500, "초대 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요."),
    HOUSEHOLD_4001(404, "소속된 그룹이 없습니다."),
    HOUSEHOLD_4002(400, "방장은 그룹을 나갈 수 없습니다. 방장을 위임하거나 그룹을 삭제해주세요."),
    HOUSEHOLD_4003(403, "방장만 수행할 수 있습니다."),
    HOUSEHOLD_4004(400, "대상 유저가 같은 그룹에 속해 있지 않습니다.");

    private final int status;
    private final String message;
}