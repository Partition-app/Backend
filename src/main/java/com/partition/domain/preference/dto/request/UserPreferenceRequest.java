package com.partition.domain.preference.dto.request;

import com.partition.entity.enums.ChoreType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserPreferenceRequest {

    @Valid
    @NotNull(message = "선호도 리스트는 필수입니다.")
    private List<PreferenceDto> preferences;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PreferenceDto {
        @NotNull(message = "집안일 종류는 필수입니다.")
        private ChoreType choreType;

        @NotNull(message = "점수는 필수입니다.")
        @Min(value = 1, message = "점수는 1점 이상이어야 합니다.")
        @Max(value = 5, message = "점수는 5점 이하이어야 합니다.")
        private Integer score;
    }
}
