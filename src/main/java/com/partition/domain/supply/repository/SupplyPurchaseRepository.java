package com.partition.domain.supply.repository;

import com.partition.entity.SupplyPurchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SupplyPurchaseRepository extends JpaRepository<SupplyPurchase, Long> {

    List<SupplyPurchase> findAllByHouseholdIdAndPurchaseDateBetweenOrderByPurchaseDateAscIdAsc(
            Long householdId,
            LocalDate startDate,
            LocalDate endDate
    );
}
