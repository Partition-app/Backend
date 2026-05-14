package com.partition.domain.utilitybill.repository;

import com.partition.entity.UtilityBill;
import com.partition.entity.UtilityBillPayment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UtilityBillPaymentRepository extends JpaRepository<UtilityBillPayment, Long> {

    Optional<UtilityBillPayment> findByBillAndYearMonth(UtilityBill bill, String yearMonth);
}
