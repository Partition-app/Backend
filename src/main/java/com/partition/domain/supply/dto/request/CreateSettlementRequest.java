package com.partition.domain.supply.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CreateSettlementRequest {

    private List<Long> purchaseIds;
    private List<Long> memberIds;
}
