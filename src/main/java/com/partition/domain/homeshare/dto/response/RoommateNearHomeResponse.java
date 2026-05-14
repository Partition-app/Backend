package com.partition.domain.homeshare.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoommateNearHomeResponse {

    private Long userId;
    private String name;
    @JsonProperty("isNearHome")
    private boolean isNearHome;
}
