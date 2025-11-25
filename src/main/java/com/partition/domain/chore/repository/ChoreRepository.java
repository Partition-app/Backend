package com.partition.domain.chore.repository;

import com.partition.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChoreRepository extends JpaRepository<Chore, Long> {

    @Query("SELECT c FROM Chore c WHERE c.assignee.householdId = :householdId AND c.date BETWEEN :startDate AND :endDate")
    List<Chore> findAllByHouseholdIdAndDateRange(
            @Param("householdId") Long householdId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}