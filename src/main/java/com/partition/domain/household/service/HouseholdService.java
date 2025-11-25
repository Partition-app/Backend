package com.partition.domain.household.service;

import com.partition.domain.chore.repository.HouseholdChoreRepository;
import com.partition.domain.household.exception.HouseholdErrorCode;
import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Household;
import com.partition.entity.HouseholdChore;
import com.partition.entity.User;
import com.partition.entity.enums.ChoreType;
import com.partition.entity.enums.UserRole;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final HouseholdChoreRepository householdChoreRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_RETRY_ATTEMPTS = 10; // 초대 코드 생성 최대 재시도 횟수
    private final SecureRandom random = new SecureRandom();

    // 그룹 생성
    @Transactional
    public Household createHousehold(Long userId, String householdName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Household household = null;

        // 경쟁 조건(Race Condition) 해결을 위한 Optimistic Loop (최대 10회 시도)
        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                String inviteCode = generateRandomString();

                household = Household.builder()
                        .name(householdName)
                        .inviteCode(inviteCode)
                        .build();

                // saveAndFlush를 사용하여 DB 제약 조건(Unique) 위반 시 즉시 예외 발생 유도
                householdRepository.saveAndFlush(household);

                // 성공 시 집안일 난이도 초기화 수행 후 루프 탈출
                initHouseholdChores(household);
                break;

            } catch (DataIntegrityViolationException e) {
                // 마지막 시도에서도 실패하면 예외 발생
                if (attempt == MAX_RETRY_ATTEMPTS - 1) {
                    throw new CustomException(HouseholdErrorCode.INVITE_CODE_GENERATION_FAILED);
                }
                // 실패 시 다음 루프에서 재시도 (inviteCode 새로 생성)
            }
        }

        // 유저 정보 업데이트 (그룹 매핑 & 방장 권한 부여)
        user.updateHousehold(household.getId(), UserRole.LEADER);

        return household;
    }

    // 그룹 참여 (초대 코드)
    @Transactional
    public Household joinHousehold(Long userId, String inviteCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 이미 그룹에 속해있는지 확인
        if (user.getHouseholdId() != null) {
            throw new CustomException(HouseholdErrorCode.ALREADY_JOINED);
        }

        // 초대 코드로 그룹 조회
        Household household = householdRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CustomException(HouseholdErrorCode.INVALID_INVITE_CODE));

        // 유저를 멤버로 등록
        user.updateHousehold(household.getId(), UserRole.MEMBER);

        return household;
    }

    // 그룹 생성 시, 기본 집안일 난이도(Default) 데이터 생성
    private void initHouseholdChores(Household household) {
        for (ChoreType type : ChoreType.values()) {
            HouseholdChore householdChore = HouseholdChore.builder()
                    .household(household)
                    .choreType(type)
                    .difficulty(type.getDefaultDifficulty()) // Enum에 정의된 기본값(3) 사용
                    .build();
            householdChoreRepository.save(householdChore);
        }
    }

    // 랜덤 문자열 생성 유틸
    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}