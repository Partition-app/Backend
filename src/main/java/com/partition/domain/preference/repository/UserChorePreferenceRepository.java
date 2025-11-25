package com.partition.domain.preference.repository;

import com.partition.entity.UserChorePreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserChorePreferenceRepository extends JpaRepository<UserChorePreference, Long> {
    // 유저의 기존 선호도 데이터를 모두 삭제 (재등록 시 초기화용)
    void deleteByUserId(Long userId);
}