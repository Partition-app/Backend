package com.partition.domain.chore.service;

import com.partition.domain.chore.dto.request.AssignmentRequest;
import com.partition.domain.chore.dto.response.AssignmentResponse;
import com.partition.domain.chore.exception.ChoreErrorCode;
import com.partition.domain.chore.repository.ChoreRepository;
import com.partition.domain.chore.repository.HouseholdChoreRepository;
import com.partition.domain.preference.repository.UserChorePreferenceRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Chore;
import com.partition.entity.HouseholdChore;
import com.partition.entity.User;
import com.partition.entity.UserChorePreference;
import com.partition.entity.enums.ChoreType;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
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

    @Value("${fastapi.url}")
    private String FASFASTAPI_URL;

    public List<AssignmentResponse.AssignmentResult> assignChores(Long userId, LocalDate startDate, int periodDays, List<ChoreType> targetChoreTypes) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (requester.getHouseholdId() == null) {
            throw new CustomException(UserErrorCode.HAVE_NO_GROUP);
        }
        Long householdId = requester.getHouseholdId();
        LocalDate endDate = startDate.plusDays(periodDays - 1);

        List<User> members = userRepository.findByHouseholdId(householdId);
        List<HouseholdChore> householdChores = householdChoreRepository.findByHouseholdId(householdId);

        if (targetChoreTypes != null && !targetChoreTypes.isEmpty()) {
            householdChores = householdChores.stream()
                    .filter(hc -> targetChoreTypes.contains(hc.getChoreType()))
                    .collect(Collectors.toList());
        }

        List<UserChorePreference> preferences = preferenceRepository.findAllByHouseholdId(householdId);

        AssignmentRequest request = createRequest(householdId, startDate, endDate, members, householdChores, preferences);

        log.info("FastAPI로 배정 요청 전송: householdId={}", householdId);
        AssignmentResponse response;
        try {
            response = restTemplate.postForObject(FASFASTAPI_URL + "/api/chores/assign", request, AssignmentResponse.class);

            if (response == null || response.getAssignments() == null) {
                throw new CustomException(ChoreErrorCode.ASSIGNMENT_API_ERROR);
            }
        } catch (RestClientException e) {
            log.error("FastAPI 호출 실패: {}", e.getMessage(), e);
            throw new CustomException(ChoreErrorCode.ASSIGNMENT_API_UNAVAILABLE);
        }

        saveAssignmentsInTransaction(response, members, householdChores);

        log.info("집안일 배정 완료: 총 {}건", response.getAssignments().size());

        return response.getAssignments();
    }

    @Transactional
    public void saveAssignmentsInTransaction(AssignmentResponse response, List<User> members, List<HouseholdChore> householdChores) {
        Map<Long, User> userMap = members.stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, HouseholdChore> choreMap = householdChores.stream().collect(Collectors.toMap(HouseholdChore::getId, c -> c));

        for (AssignmentResponse.AssignmentResult result : response.getAssignments()) {
            User assignee = userMap.get(result.getUserId());
            HouseholdChore hhChore = choreMap.get(result.getChoreId());

            if (assignee == null || hhChore == null) {
                log.warn("유효하지 않은 배정 결과 건너뜀: userId={}, choreId={}", result.getUserId(), result.getChoreId());
                continue;
            }

            Chore chore = Chore.builder()
                    .assignee(assignee)
                    .type(hhChore.getChoreType())
                    .date(result.getDate())
                    .build();

            choreRepository.save(chore);
        }
    }

    private AssignmentRequest createRequest(Long householdId, LocalDate startDate, LocalDate endDate,
                                            List<User> members, List<HouseholdChore> chores, List<UserChorePreference> preferences) {

        List<AssignmentRequest.UserDto> userDtos = members.stream()
                .map(u -> new AssignmentRequest.UserDto(u.getId(), u.getName()))
                .toList();

        List<AssignmentRequest.ChoreInfo> choreDtos = chores.stream()
                .map(c -> new AssignmentRequest.ChoreInfo(
                        c.getId(),
                        c.getChoreType().name(),
                        c.getDifficulty(),
                        c.getFrequency()
                ))
                .toList();

        Map<String, Long> typeToIdMap = chores.stream()
                .collect(Collectors.toMap(
                        c -> c.getChoreType().name(),
                        HouseholdChore::getId,
                        (existing, replacement) -> existing
                ));

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
}