package com.partition.domain.household.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HouseholdInfoResponse {

    private Long householdId;
    private String householdName;
    private String inviteCode;
    private Boolean isLeader;
    private List<HouseholdMemberResponse> members;
}