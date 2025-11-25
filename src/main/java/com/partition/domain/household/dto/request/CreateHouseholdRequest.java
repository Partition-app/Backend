package com.partition.domain.household.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateHouseholdRequest {

    @NotBlank(message = "그룹 이름은 필수입니다.")
    private String name;
}
