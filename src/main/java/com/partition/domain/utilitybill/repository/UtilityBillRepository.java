package com.partition.domain.utilitybill.repository;

import com.partition.entity.UtilityBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long> {

    // 공과금은 Household와 직접 연관됨
    List<UtilityBill> findAllByHouseholdIdAndDueDateBetween(Long householdId, LocalDate startDate, LocalDate endDate);
}