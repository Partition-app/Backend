package com.partition.domain.reservation.repository;

import com.partition.entity.Reservation;
import com.partition.entity.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.item = :item AND r.startTime < :endTime AND r.endTime > :startTime")
    boolean existsOverlap(
            @Param("item") ReservationItem item,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
