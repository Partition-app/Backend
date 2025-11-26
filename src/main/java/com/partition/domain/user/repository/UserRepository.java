package com.partition.domain.user.repository;

import com.partition.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 소셜 로그인용 조회
    Optional<User> findByProviderId(String providerId);

    // 이메일 조회 (필요 시)
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 특정 그룹에 속한 모든 멤버 조회 (집안일 배정 시 사용)
    List<User> findByHouseholdId(Long householdId);

}