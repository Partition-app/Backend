package com.partition.domain.household.service;

import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Household;
import com.partition.entity.User;
import com.partition.entity.enums.UserRole;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;

    // 초대 코드 생성용 상수 (영문 대문자 + 숫자)
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public Household createHousehold(Long userId, String householdName) {
        // 1. 유저 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 2. 중복되지 않는 초대 코드 생성
        String inviteCode = generateUniqueInviteCode();

        // 3. 그룹(Household) 생성 및 저장
        Household household = Household.builder()
                .name(householdName)
                .inviteCode(inviteCode)
                .build();

        householdRepository.save(household);

        // 4. 유저 정보 업데이트 (그룹 매핑 & 방장 권한 부여)
        // User 엔티티에 updateHousehold 메서드가 있어야 동작합니다.
        user.updateHousehold(household.getId(), UserRole.LEADER);

        return household;
    }

    // 랜덤 코드 생성 로직 (중복 시 재시도)
    private String generateUniqueInviteCode() {
        String code;
        do {
            code = generateRandomString();
        } while (householdRepository.existsByInviteCode(code));
        return code;
    }

    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}