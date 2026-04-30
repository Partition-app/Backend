package com.partition.domain.reservation.repository;

import com.partition.entity.Household;
import com.partition.entity.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {

    boolean existsByHouseholdAndName(Household household, String name);
}
