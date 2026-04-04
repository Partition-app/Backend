package com.partition.domain.schedule.repository;

import com.partition.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 해당 그룹(Household)에 속한 모든 유저의 일정 조회
    // User -> Household 관계를 통해 조인
    @Query("SELECT s FROM Schedule s WHERE s.user.householdId = :householdId AND s.date BETWEEN :startDate AND :endDate")
    List<Schedule> findAllByHouseholdIdAndDateRange(
            @Param("householdId") Long householdId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 일간 상세 조회용 (특정 날짜)
    @Query("SELECT s FROM Schedule s JOIN FETCH s.user WHERE s.user.householdId = :householdId AND s.date = :date")
    List<Schedule> findAllByHouseholdIdAndDate(
            @Param("householdId") Long householdId,
            @Param("date") LocalDate date
    );
}