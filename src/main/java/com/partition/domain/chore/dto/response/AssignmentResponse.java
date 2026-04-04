package com.partition.domain.chore.dto.response;

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
        private Long choreId;   // HouseholdChore의 ID (설정값 ID)
        private LocalDate date; // "2025-11-21" 문자열이 LocalDate로 자동 매핑됨
    }
}
