package com.partition.domain.chore.repository;

import com.partition.entity.HouseholdChore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HouseholdChoreRepository extends JpaRepository<HouseholdChore, Long> {
    List<HouseholdChore> findByHouseholdId(Long householdId);
}