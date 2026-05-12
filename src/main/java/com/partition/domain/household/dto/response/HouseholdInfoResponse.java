package com.partition.domain.household.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HouseholdInfoResponse {

    private Long householdId;
    private String householdName;
    private Boolean isLeader;
}