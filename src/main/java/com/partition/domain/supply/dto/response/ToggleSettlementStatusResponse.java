package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ToggleSettlementStatusResponse {

    private Long purchaseId;
    private String itemName;
    private String status;
}