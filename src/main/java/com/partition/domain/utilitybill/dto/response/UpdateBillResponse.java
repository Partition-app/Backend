package com.partition.domain.utilitybill.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateBillResponse {

    private Long billId;
    private String utilityType;
    private String utilityTypeName;
    private Integer payDay;
    @JsonProperty("isFixed")
    private boolean isFixed;
    private Integer amount;
    private String note;
}
