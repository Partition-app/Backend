package com.partition.domain.utilitybill.repository;

import com.partition.entity.Settlement;
import com.partition.entity.UtilityBill;
import com.partition.entity.UtilityBillPayment;
import com.partition.entity.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UtilityBillPaymentRepository extends JpaRepository<UtilityBillPayment, Long> {

    Optional<UtilityBillPayment> findByBillAndYearMonth(UtilityBill bill, String yearMonth);

    List<UtilityBillPayment> findAllByBillInAndYearMonth(List<UtilityBill> bills, String yearMonth);

    @Query("SELECT p FROM UtilityBillPayment p WHERE p.bill.household.id = :householdId AND p.status = :status")
    List<UtilityBillPayment> findAllByHouseholdIdAndStatus(@Param("householdId") Long householdId, @Param("status") BillStatus status);

    @Query("SELECT p FROM UtilityBillPayment p WHERE p.id IN :ids AND p.bill.household.id = :householdId")
    List<UtilityBillPayment> findAllByIdInAndHouseholdId(@Param("ids") List<Long> ids, @Param("householdId") Long householdId);

    List<UtilityBillPayment> findAllBySettlement(Settlement settlement);
}
