package com.partition.domain.utilitybill.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CreateBillResponse {

    private Long billId;
    private String utilityType;
    private String utilityTypeName;
    private Integer payDay;
    @JsonProperty("isFixed")
    private boolean isFixed;
    private Integer amount;
    private Integer thisMonthAmount;
    private String note;
    private String status;
    private LocalDateTime createdAt;
}
