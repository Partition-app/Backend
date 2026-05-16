package com.partition.domain.chore.dto.request;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UpdateChoreRequest {

    private Long assigneeId;
    private LocalDate date;
}
