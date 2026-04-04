package com.partition.domain.supply.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SupplySubCategoryResponse {

    private String subCategory;
    private String subCategoryName;
}
