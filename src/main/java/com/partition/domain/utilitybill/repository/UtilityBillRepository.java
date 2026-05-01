package com.partition.domain.utilitybill.repository;

import com.partition.entity.UtilityBill;
import com.partition.entity.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long> {

    List<UtilityBill> findAllByHouseholdIdOrderByIdAsc(Long householdId);

    List<UtilityBill> findAllByHouseholdIdAndDueDateBetween(Long householdId, LocalDate startDate, LocalDate endDate);

    List<UtilityBill> findAllByHouseholdIdAndStatusAndDueDateBetweenOrderByDueDateAscIdAsc(
            Long householdId, BillStatus status, LocalDate startDate, LocalDate endDate);

    List<UtilityBill> findAllBySettlementId(Long settlementId);

    List<UtilityBill> findAllByHouseholdIdAndStatus(Long householdId, BillStatus status);
}