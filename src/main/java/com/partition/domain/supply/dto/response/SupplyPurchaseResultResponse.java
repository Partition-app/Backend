package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class SupplyPurchaseResultResponse {

    private Long purchaseId;
    private String itemName;
    private LocalDate purchaseDate;
    private Integer amount;
    private Integer quantity;
    private Boolean isSettled;
}
