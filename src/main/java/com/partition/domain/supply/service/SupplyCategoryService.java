package com.partition.domain.supply.service;

import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.supply.dto.response.SupplyCategoryResponse;
import com.partition.domain.supply.dto.response.SupplySubCategoryResponse;
import com.partition.domain.supply.exception.SupplyErrorCode;
import com.partition.domain.supply.repository.SupplyCategoryRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Household;
import com.partition.entity.SupplyCategory;
import com.partition.entity.User;
import com.partition.entity.enums.SupplyCategoryType;
import com.partition.entity.enums.SupplySubCategoryType;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplyCategoryService {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final SupplyCategoryRepository supplyCategoryRepository;

    public void initializeDefaultCategories(Household household) {
        for (SupplySubCategoryType subCategoryType : SupplySubCategoryType.defaults()) {
            supplyCategoryRepository.save(
                    SupplyCategory.builder()
                            .household(household)
                            .category(subCategoryType.getCategoryType())
                            .subCategory(subCategoryType)
                            .build()
            );
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void syncMissingCategories() {
        List<Household> households = householdRepository.findAll();
        Set<SupplySubCategoryType> allSubCategories = Set.of(SupplySubCategoryType.values());

        for (Household household : households) {
            Set<SupplySubCategoryType> existing = supplyCategoryRepository
                    .findAllByHouseholdIdOrderByCategoryAscSubCategoryAsc(household.getId())
                    .stream()
                    .map(SupplyCategory::getSubCategory)
                    .collect(Collectors.toSet());

            for (SupplySubCategoryType subCategoryType : allSubCategories) {
                if (!existing.contains(subCategoryType)) {
                    supplyCategoryRepository.save(
                            SupplyCategory.builder()
                                    .household(household)
                                    .category(subCategoryType.getCategoryType())
                                    .subCategory(subCategoryType)
                                    .build()
                    );
                }
            }
        }
    }

    @Transactional(readOnly = true)
    public List<SupplyCategoryResponse> getCategories(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3007));

        if (user.getHouseholdId() == null) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3008);
        }

        householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3008));

        List<SupplyCategory> categories = supplyCategoryRepository
                .findAllByHouseholdIdOrderByCategoryAscSubCategoryAsc(user.getHouseholdId());

        if (categories.isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_1005);
        }

        Map<SupplyCategoryType, List<SupplyCategory>> categoriesByParent = categories.stream()
                .collect(Collectors.groupingBy(SupplyCategory::getCategory));

        return categoriesByParent.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Enum::ordinal)))
                .map(entry -> SupplyCategoryResponse.builder()
                        .category(entry.getKey().name())
                        .categoryName(entry.getKey().getLabel())
                        .subCategories(
                                entry.getValue().stream()
                                        .map(category -> SupplySubCategoryResponse.builder()
                                                .subCategory(category.getSubCategory().name())
                                                .subCategoryName(category.getSubCategory().getLabel())
                                                .build())
                                        .toList()
                        )
                        .build())
                .toList();
    }
}
