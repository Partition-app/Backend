package com.partition.domain.supply.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptItemResponse {
    private String itemName;
    private String purchaseDate;
    private Integer amount;
    private Integer quantity;
    private String category;
    private String subCategory;
}