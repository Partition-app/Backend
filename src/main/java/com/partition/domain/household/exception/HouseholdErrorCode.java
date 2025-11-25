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
    INVITE_CODE_GENERATION_FAILED(500, "초대 코드 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final int status;
    private final String message;
}