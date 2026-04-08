package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SettlementListResponse {

    private int totalCount;
    private int totalAmount;
    private int amountPerMember;
    private int memberCount;
    private List<SettlementPurchaseResponse> purchases;
}
