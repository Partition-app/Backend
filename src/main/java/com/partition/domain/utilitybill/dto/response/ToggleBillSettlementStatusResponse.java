package com.partition.domain.utilitybill.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ToggleBillSettlementStatusResponse {

    private Long paymentId;
    private Long billId;
    private String utilityType;
    private String utilityTypeName;
    private String status;
}
