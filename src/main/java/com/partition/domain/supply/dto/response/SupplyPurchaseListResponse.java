package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SupplyPurchaseListResponse {

    private Integer totalCount;
    private List<SupplyPurchaseResultResponse> purchases;
}
