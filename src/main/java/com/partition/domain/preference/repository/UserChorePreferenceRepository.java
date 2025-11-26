package com.partition.domain.preference.repository;

import com.partition.entity.UserChorePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface UserChorePreferenceRepository extends JpaRepository<UserChorePreference, Long> {
    // 유저의 기존 선호도 데이터를 모두 삭제 (재등록 시 초기화용)
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);

    @Query("SELECT ucp FROM UserChorePreference ucp WHERE ucp.user.householdId = :householdId")
    List<UserChorePreference> findAllByHouseholdId(@Param("householdId") Long householdId);
}
