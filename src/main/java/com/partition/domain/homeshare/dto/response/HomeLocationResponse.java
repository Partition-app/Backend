package com.partition.domain.homeshare.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HomeLocationResponse {

    private Long householdId;
    private double lat;
    private double lng;
    private int radius;
}
