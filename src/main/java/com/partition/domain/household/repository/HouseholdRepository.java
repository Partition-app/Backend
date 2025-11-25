package com.partition.domain.household.repository;

import com.partition.entity.Household;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HouseholdRepository extends JpaRepository<Household, Long> {
    // 초대 코드 중복 체크를 위한 메서드
    boolean existsByInviteCode(String inviteCode);
    Optional<Household> findByInviteCode(String inviteCode);
}