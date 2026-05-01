package com.partition.domain.utilitybill.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateBillResponse {

    private Long billId;
    private String utilityType;
    private String utilityTypeName;
    private Integer payDay;
    private Integer amount;
    private String note;
    private String status;
}
