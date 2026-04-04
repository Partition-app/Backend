package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SupplyCategoryResponse {

    private String category;
    private String categoryName;
    private List<SupplySubCategoryResponse> subCategories;
}
