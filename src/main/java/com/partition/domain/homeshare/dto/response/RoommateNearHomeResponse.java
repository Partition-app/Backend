package com.partition.domain.homeshare.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoommateNearHomeResponse {

    private Long userId;
    private String name;
    private boolean isNearHome;
}
