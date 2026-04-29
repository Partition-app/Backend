package com.partition.domain.chore.repository;

import com.partition.entity.Chore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ChoreRepository extends JpaRepository<Chore, Long> {

    @Modifying
    @Query("DELETE FROM Chore c WHERE c.assignee.householdId = :householdId AND c.date BETWEEN :startDate AND :endDate")
    void deleteAllByHouseholdIdAndDateRange(
            @Param("householdId") Long householdId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("SELECT c FROM Chore c WHERE c.assignee.householdId = :householdId AND c.date BETWEEN :startDate AND :endDate")
    List<Chore> findAllByHouseholdIdAndDateRange(
            @Param("householdId") Long householdId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 일간 상세 조회용 (특정 날짜) - assignee 정보 함께 가져오기(Fetch Join)
    @Query("SELECT c FROM Chore c JOIN FETCH c.assignee WHERE c.assignee.householdId = :householdId AND c.date = :date")
    List<Chore> findAllByHouseholdIdAndDate(
            @Param("householdId") Long householdId,
            @Param("date") LocalDate date
    );
}