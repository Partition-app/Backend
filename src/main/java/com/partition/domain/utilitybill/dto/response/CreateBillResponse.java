package com.partition.domain.utilitybill.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CreateBillResponse {

    private Long billId;
    private String billType;
    private String billTypeName;
    private LocalDate dueDate;
    private Integer amount;
    private LocalDateTime createdAt;
}
