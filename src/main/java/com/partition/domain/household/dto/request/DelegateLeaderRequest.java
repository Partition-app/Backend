package com.partition.domain.household.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DelegateLeaderRequest {

    @NotNull(message = "위임할 대상 유저 ID는 필수입니다.")
    private Long targetUserId;
}