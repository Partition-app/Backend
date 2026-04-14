package com.partition.domain.utilitybill.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BillCategoryResponse {

    private String category;
    private String categoryName;
}