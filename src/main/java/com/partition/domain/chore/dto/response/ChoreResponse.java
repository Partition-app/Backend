package com.partition.domain.chore.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.partition.entity.Chore;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ChoreResponse {

    private Long choreId;
    private Long assigneeId;
    private String assigneeName;
    private String choreType;
    private String choreName;
    private LocalDate date;

    @JsonProperty("isCompleted")
    private boolean isCompleted;

    public static ChoreResponse from(Chore chore) {
        return ChoreResponse.builder()
                .choreId(chore.getId())
                .assigneeId(chore.getAssignee().getId())
                .assigneeName(chore.getAssignee().getName())
                .choreType(chore.getType().name())
                .choreName(chore.getType().getDescription())
                .date(chore.getDate())
                .isCompleted(chore.isCompleted())
                .build();
    }
}
