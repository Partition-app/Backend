package com.partition.domain.preference.repository;

import com.partition.entity.UserChorePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface UserChorePreferenceRepository extends JpaRepository<UserChorePreference, Long> {
    // 유저의 기존 선호도 데이터를 모두 삭제 (재등록 시 초기화용)
    @Modifying // [추가] 데이터 변경 작업임을 명시
    @Transactional // [추가] 트랜잭션 필수 (삭제 도중 실패하면 롤백)
    void deleteByUserId(Long userId);
}