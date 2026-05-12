package com.partition.domain.household.service;

import com.partition.domain.chore.repository.HouseholdChoreRepository;
import com.partition.domain.household.dto.response.HouseholdInfoResponse;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class HouseholdService {

    private final HouseholdRepository householdRepository;
    private final UserRepository userRepository;
    private final HouseholdChoreRepository householdChoreRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_RETRY_ATTEMPTS = 10;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public Household createHousehold(Long userId, String householdName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() != null) {
            throw new CustomException(HouseholdErrorCode.ALREADY_JOINED);
        }

        Household household = null;

        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                household = Household.builder()
                        .name(householdName)
                        .inviteCode(generateRandomString())
                        .build();

                householdRepository.saveAndFlush(household);
                initHouseholdChores(household);
                break;
            } catch (DataIntegrityViolationException e) {
                if (attempt == MAX_RETRY_ATTEMPTS - 1) {
                    throw new CustomException(HouseholdErrorCode.INVITE_CODE_GENERATION_FAILED);
                }
            }
        }

        user.updateHousehold(household.getId(), UserRole.LEADER);
        return household;
    }

    @Transactional
    public void updateHouseholdName(Long userId, String name) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            throw new CustomException(HouseholdErrorCode.HOUSEHOLD_4001);
        }

        if (user.getMemberRole() != UserRole.LEADER) {
            throw new CustomException(HouseholdErrorCode.HOUSEHOLD_4003);
        }

        Household household = householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(HouseholdErrorCode.HOUSEHOLD_NOT_FOUND));

        household.updateName(name);
    }

    @Transactional(readOnly = true)
    public HouseholdInfoResponse getHouseholdInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            throw new CustomException(HouseholdErrorCode.HOUSEHOLD_4001);
        }

        Household household = householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(HouseholdErrorCode.HOUSEHOLD_NOT_FOUND));

        return HouseholdInfoResponse.builder()
                .householdId(household.getId())
                .householdName(household.getName())
                .isLeader(user.getMemberRole() == UserRole.LEADER)
                .build();
    }

    @Transactional(readOnly = true)
    public List<User> getMembers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            throw new CustomException(HouseholdErrorCode.HOUSEHOLD_4001);
        }

        return userRepository.findByHouseholdId(user.getHouseholdId());
    }

    @Transactional
    public void delegateLeader(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getMemberRole() != UserRole.LEADER) {
            throw new CustomException(HouseholdErrorCode.HOUSEHOLD_4003);
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (!user.getHouseholdId().equals(target.getHouseholdId())) {
            throw new CustomException(HouseholdErrorCode.HOUSEHOLD_4004);
        }

        user.updateHousehold(user.getHouseholdId(), UserRole.MEMBER);
        target.updateHousehold(target.getHouseholdId(), UserRole.LEADER);
    }

    @Transactional
    public void leaveHousehold(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            throw new CustomException(HouseholdErrorCode.HOUSEHOLD_4001);
        }

        if (user.getMemberRole() == UserRole.LEADER) {
            throw new CustomException(HouseholdErrorCode.HOUSEHOLD_4002);
        }

        user.updateHousehold(null, UserRole.GUEST);
    }

    @Transactional
    public Household joinHousehold(Long userId, String inviteCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() != null) {
            throw new CustomException(HouseholdErrorCode.ALREADY_JOINED);
        }

        Household household = householdRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new CustomException(HouseholdErrorCode.INVALID_INVITE_CODE));

        user.updateHousehold(household.getId(), UserRole.MEMBER);
        return household;
    }

    private void initHouseholdChores(Household household) {
        for (ChoreType type : ChoreType.values()) {
            HouseholdChore householdChore = HouseholdChore.builder()
                    .household(household)
                    .choreType(type)
                    .difficulty(type.getDefaultDifficulty())
                    .frequency(type.getDefaultFrequency())
                    .build();
            householdChoreRepository.save(householdChore);
        }
    }

    private String generateRandomString() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
