package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CreateSupplyPurchaseResponse {

    private Long purchaseId;
    private String itemName;
    private LocalDate purchaseDate;
    private Integer amount;
    private Integer quantity;
    private String category;
    private String subCategory;
    private LocalDateTime createdAt;
}
