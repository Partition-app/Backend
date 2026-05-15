package com.partition.domain.utilitybill.repository;

import com.partition.entity.UtilityBill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UtilityBillRepository extends JpaRepository<UtilityBill, Long> {

    List<UtilityBill> findAllByHouseholdIdOrderByIdAsc(Long householdId);

    List<UtilityBill> findAllByIsFixedFalseAndPayDay(int payDay);
}
