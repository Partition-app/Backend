package com.partition.domain.supply.service;

import com.partition.domain.supply.dto.response.SupplyCategoryResponse;
import com.partition.domain.supply.dto.response.SupplySubCategoryResponse;
import com.partition.domain.supply.exception.SupplyErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.User;
import com.partition.entity.enums.SupplyCategoryType;
import com.partition.entity.enums.SupplySubCategoryType;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplyCategoryService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<SupplyCategoryResponse> getCategories(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3007));

        if (user.getHouseholdId() == null) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3008);
        }

        Map<SupplyCategoryType, List<SupplySubCategoryType>> grouped = Arrays.stream(SupplySubCategoryType.values())
                .collect(Collectors.groupingBy(SupplySubCategoryType::getCategoryType));

        return Arrays.stream(SupplyCategoryType.values())
                .filter(grouped::containsKey)
                .sorted(Comparator.comparingInt(Enum::ordinal))
                .map(categoryType -> SupplyCategoryResponse.builder()
                        .category(categoryType.name())
                        .categoryName(categoryType.getLabel())
                        .subCategories(grouped.get(categoryType).stream()
                                .map(sub -> SupplySubCategoryResponse.builder()
                                        .subCategory(sub.name())
                                        .subCategoryName(sub.getLabel())
                                        .build())
                                .toList())
                        .build())
                .toList();
    }
}