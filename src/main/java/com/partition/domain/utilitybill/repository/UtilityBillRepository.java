package com.partition.domain.utilitybill.repository;

import com.partition.entity.UtilityBill;
import com.partition.entity.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long> {

    List<UtilityBill> findAllByHouseholdIdOrderByIdAsc(Long householdId);

    List<UtilityBill> findAllByHouseholdIdAndCreatedAtBetween(Long householdId, LocalDateTime start, LocalDateTime end);

    List<UtilityBill> findAllByHouseholdIdAndStatusAndCreatedAtBetweenOrderByCreatedAtAscIdAsc(
            Long householdId, BillStatus status, LocalDateTime start, LocalDateTime end);

    List<UtilityBill> findAllBySettlementId(Long settlementId);

    List<UtilityBill> findAllByHouseholdIdAndStatus(Long householdId, BillStatus status);
}
