package com.partition.entity.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {
    LEADER("ROLE_LEADER", "방장"),
    MEMBER("ROLE_MEMBER", "구성원"),
    GUEST("ROLE_GUEST", "게스트"); // 집에 소속되지 않은 상태

    private final String key;
    private final String title;
}