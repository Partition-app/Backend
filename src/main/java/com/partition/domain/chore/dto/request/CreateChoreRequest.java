package com.partition.domain.chore.dto.request;

import com.partition.entity.enums.ChoreType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateChoreRequest {

    @NotNull
    private Long assigneeId;

    @NotNull
    private ChoreType choreType;

    @NotNull
    private LocalDate date;
}
