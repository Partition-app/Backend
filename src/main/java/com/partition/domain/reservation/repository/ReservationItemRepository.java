package com.partition.domain.reservation.repository;

import com.partition.entity.Household;
import com.partition.entity.ReservationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {

    boolean existsByHouseholdAndName(Household household, String name);

    List<ReservationItem> findByHousehold(Household household);

    boolean existsByHouseholdAndNameAndIdNot(Household household, String name, Long id);

    List<ReservationItem> findAllByIdInAndHousehold(List<Long> ids, Household household);
}
