package com.partition.domain.supply.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateSupplyPurchaseRequest {

    private String itemName;
    private String purchaseDate;
    private Integer amount;
    private Integer quantity;
    private String category;
    private String subCategory;
}
