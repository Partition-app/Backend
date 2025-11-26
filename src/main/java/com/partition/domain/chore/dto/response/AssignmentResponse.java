package com.partition.domain.chore.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@NoArgsConstructor
public class AssignmentResponse {

    private Long householdId; // [추가] 응답 최상단에 householdId 포함
    private List<AssignmentResult> assignments;

    @Getter
    @NoArgsConstructor
    public static class AssignmentResult {
        private Long userId;
        private Long choreId;   // HouseholdChore의 ID (설정값 ID)
        private LocalDate date; // "2025-11-21" 문자열이 LocalDate로 자동 매핑됨
    }
}
