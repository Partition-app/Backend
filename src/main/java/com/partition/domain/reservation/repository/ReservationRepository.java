package com.partition.domain.reservation.repository;

import com.partition.entity.Reservation;
import com.partition.entity.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.item = :item AND r.startTime < :endTime AND r.endTime > :startTime")
    boolean existsOverlap(
            @Param("item") ReservationItem item,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.item = :item AND r.id <> :excludeId AND r.startTime < :endTime AND r.endTime > :startTime")
    boolean existsOverlapExcluding(
            @Param("item") ReservationItem item,
            @Param("excludeId") Long excludeId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    List<Reservation> findAllByIdIn(List<Long> ids);

    @Query("SELECT r FROM Reservation r WHERE r.item.household.id = :householdId AND r.startTime < :endDateTime AND r.endTime > :startDateTime ORDER BY r.startTime ASC")
    List<Reservation> findByHouseholdIdAndDateRange(
            @Param("householdId") Long householdId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );
}
