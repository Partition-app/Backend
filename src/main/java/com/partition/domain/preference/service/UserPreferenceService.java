package com.partition.domain.preference.service;

import com.partition.domain.preference.dto.request.UserPreferenceRequest;
import com.partition.domain.preference.repository.UserChorePreferenceRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.User;
import com.partition.entity.UserChorePreference;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserChorePreferenceRepository preferenceRepository;

    @Transactional
    public List<UserPreferenceRequest.PreferenceDto> savePreferences(Long userId, UserPreferenceRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // 중복 방지를 위해 해당 유저의 기존 선호도 데이터 삭제
        preferenceRepository.deleteByUserId(userId);

        List<UserPreferenceRequest.PreferenceDto> savedList = new ArrayList<>();

        // 새로운 선호도 리스트 저장
        for (UserPreferenceRequest.PreferenceDto dto : request.getPreferences()) {
            UserChorePreference preference = UserChorePreference.builder()
                    .user(user)
                    .choreType(dto.getChoreType())
                    .score(dto.getScore())
                    .build();

            preferenceRepository.save(preference);
            savedList.add(dto); // 반환할 리스트에 추가
        }

        return savedList; // 저장된 결과 그대로 반환
    }
}