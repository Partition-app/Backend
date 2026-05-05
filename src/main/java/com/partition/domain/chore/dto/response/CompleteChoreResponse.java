package com.partition.domain.chore.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CompleteChoreResponse {

    private Long choreId;

    @JsonProperty("isCompleted")
    private boolean isCompleted;
}
