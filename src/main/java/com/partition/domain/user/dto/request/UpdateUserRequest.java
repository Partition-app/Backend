package com.partition.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateUserRequest {

    @NotBlank(message = "이름은 필수입니다.")
    private String name; // 변경할 닉네임
}