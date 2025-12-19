package com.partition.domain.chore.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class AssignmentResponse {


    private Long householdId;
    private List<AssignmentResult> assignments;

    @Getter
    @NoArgsConstructor
    @Setter
    public static class AssignmentResult {
        private Long userId;
        private Long choreId;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate date;    }
}
