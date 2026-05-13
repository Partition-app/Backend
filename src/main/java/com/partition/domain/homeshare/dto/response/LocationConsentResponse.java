package com.partition.domain.homeshare.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LocationConsentResponse {

    private Long userId;
    private Long householdId;
    private boolean agreed;
}
