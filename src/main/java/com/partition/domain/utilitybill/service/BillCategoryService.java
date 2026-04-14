package com.partition.domain.utilitybill.service;

import com.partition.domain.utilitybill.dto.response.BillCategoryResponse;
import com.partition.entity.enums.BillCategoryType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class BillCategoryService {

    public List<BillCategoryResponse> getCategories() {
        return Arrays.stream(BillCategoryType.values())
                .map(type -> BillCategoryResponse.builder()
                        .category(type.name())
                        .categoryName(type.getLabel())
                        .build())
                .toList();
    }
}
