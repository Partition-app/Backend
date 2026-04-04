package com.partition.domain.supply.repository;

import com.partition.entity.SupplyCategory;
import com.partition.entity.enums.SupplyCategoryType;
import com.partition.entity.enums.SupplySubCategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupplyCategoryRepository extends JpaRepository<SupplyCategory, Long> {

    List<SupplyCategory> findAllByHouseholdIdOrderByCategoryAscSubCategoryAsc(Long householdId);

    Optional<SupplyCategory> findByHouseholdIdAndCategoryAndSubCategory(
            Long householdId,
            SupplyCategoryType category,
            SupplySubCategoryType subCategory
    );
}
