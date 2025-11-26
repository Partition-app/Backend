package com.partition.domain.chore.service;

import com.partition.domain.chore.dto.request.AssignmentRequest;
import com.partition.domain.chore.dto.response.AssignmentResponse;
import com.partition.domain.chore.repository.ChoreRepository;
import com.partition.domain.chore.repository.HouseholdChoreRepository;
import com.partition.domain.preference.repository.UserChorePreferenceRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Chore;
import com.partition.entity.HouseholdChore;
import com.partition.entity.User;
import com.partition.entity.UserChorePreference;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChoreAssignmentService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final HouseholdChoreRepository householdChoreRepository;
    private final UserChorePreferenceRepository preferenceRepository;
    private final ChoreRepository choreRepository;

    private static final String FASTAPI_URL = "http://3.27.77.244:8000/api/assign-chores";

    @Transactional
    public void assignChores(Long userId, LocalDate startDate, int periodDays) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (requester.getHouseholdId() == null) {
            throw new CustomException(UserErrorCode.HAVE_NO_GROUP);
        }
        Long householdId = requester.getHouseholdId();
        LocalDate endDate = startDate.plusDays(periodDays - 1);

        List<User> members = userRepository.findByHouseholdId(householdId);
        List<HouseholdChore> householdChores = householdChoreRepository.findByHouseholdId(householdId);
        List<UserChorePreference> preferences = preferenceRepository.findAllByHouseholdId(householdId);

        // DTO 생성
        AssignmentRequest request = createRequest(householdId, startDate, endDate, members, householdChores, preferences);

        // FastAPI 호출
        AssignmentResponse response = restTemplate.postForObject(FASTAPI_URL, request, AssignmentResponse.class);

        if (response == null || response.getAssignments() == null) {
            throw new RuntimeException("FastAPI 응답 오류");
        }

        // [수정] householdId 검증 로직 추가 (선택사항)
        if (!householdId.equals(response.getHouseholdId())) {
            log.warn("요청한 그룹({})과 응답 그룹({})이 일치하지 않습니다.", householdId, response.getHouseholdId());
        }

        saveAssignments(response, members, householdChores);
    }

    private AssignmentRequest createRequest(Long householdId, LocalDate startDate, LocalDate endDate,
                                                  List<User> members, List<HouseholdChore> chores, List<UserChorePreference> preferences) {

        // 1. Users 매핑
        List<AssignmentRequest.UserDto> userDtos = members.stream()
                .map(u -> new AssignmentRequest.UserDto(u.getId(), u.getName()))
                .toList();

        // 2. Chores 매핑 (frequency: int 그대로 전송)
        List<AssignmentRequest.ChoreInfo> choreDtos = chores.stream()
                .map(c -> new AssignmentRequest.ChoreInfo(
                        c.getId(),
                        c.getChoreType().name(),
                        c.getDifficulty(),
                        c.getFrequency()
                ))
                .toList();

        // 3. Preferences 매핑
        Map<String, Long> typeToIdMap = chores.stream()
                .collect(Collectors.toMap(c -> c.getChoreType().name(), HouseholdChore::getId));

        List<AssignmentRequest.PreferenceDto> prefDtos = preferences.stream()
                .filter(p -> typeToIdMap.containsKey(p.getChoreType().name()))
                .map(p -> new AssignmentRequest.PreferenceDto(
                        p.getUser().getId(),
                        typeToIdMap.get(p.getChoreType().name()),
                        p.getScore()
                ))
                .toList();

        return AssignmentRequest.builder()
                .householdId(householdId)
                .startDate(startDate)
                .endDate(endDate)
                .users(userDtos)
                .chores(choreDtos)
                .preferences(prefDtos)
                .build();
    }

    // 응답 처리 로직 업데이트 (날짜 그대로 사용)
    private void saveAssignments(AssignmentResponse response, List<User> members, List<HouseholdChore> householdChores) {
        // 빠른 검색을 위한 Map 변환
        Map<Long, User> userMap = members.stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, HouseholdChore> choreMap = householdChores.stream().collect(Collectors.toMap(HouseholdChore::getId, c -> c));

        for (AssignmentResponse.AssignmentResult result : response.getAssignments()) {
            User assignee = userMap.get(result.getUserId());
            HouseholdChore hhChore = choreMap.get(result.getChoreId()); // 설정값(HouseholdChore) 조회

            if (assignee == null || hhChore == null) {
                log.warn("유효하지 않은 배정 결과 건너뜀: userId={}, choreId={}", result.getUserId(), result.getChoreId());
                continue;
            }

            // 실제 수행할 집안일(Chore) 엔티티 생성
            Chore chore = Chore.builder()
                    .assignee(assignee)
                    .type(hhChore.getChoreType()) // Enum 타입 설정
                    .date(result.getDate())       // FastAPI가 준 날짜 그대로 저장
                    .build();

            choreRepository.save(chore);
        }
    }
}