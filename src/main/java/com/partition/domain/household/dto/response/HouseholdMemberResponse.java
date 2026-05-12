package com.partition.domain.household.dto.response;

import com.partition.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HouseholdMemberResponse {

    private Long userId;
    private String name;
    private String profileImage;
    private String role;

    public static HouseholdMemberResponse from(User user) {
        return HouseholdMemberResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .profileImage(user.getProfileImage())
                .role(user.getMemberRole().name())
                .build();
    }
}
