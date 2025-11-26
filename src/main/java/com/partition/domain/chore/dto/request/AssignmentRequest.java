package com.partition.domain.chore.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentRequest {
    private Long householdId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<UserDto> users;
    private List<ChoreInfo> chores;
    private List<PreferenceDto> preferences;

    @Getter
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String name;
    }

    @Getter
    @AllArgsConstructor
    public static class ChoreInfo {
        private Long id;           // HouseholdChore PK
        private String name;       // DISH_WASHING
        private int difficulty;    // 1~5
        private int frequency;
    }

    @Getter
    @AllArgsConstructor
    public static class PreferenceDto {
        private Long userId;
        private Long choreId;      // HouseholdChore PK
        private int preference;    // 1~5
    }
}