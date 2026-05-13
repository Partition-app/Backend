package com.partition.domain.homeshare.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NearHomeEventResponse {

    private int notifiedUserCount;
}
