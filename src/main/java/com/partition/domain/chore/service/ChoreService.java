package com.partition.domain.chore.service;

import com.partition.domain.chore.dto.request.CreateChoreRequest;
import com.partition.domain.chore.dto.request.UpdateChoreRequest;
import com.partition.domain.chore.dto.response.ChoreResponse;
import com.partition.domain.chore.dto.response.CompleteChoreResponse;
import com.partition.domain.chore.exception.ChoreErrorCode;
import com.partition.domain.chore.repository.ChoreRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Chore;
import com.partition.entity.User;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChoreResponse createChore(Long userId, CreateChoreRequest request) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        User assignee = userRepository.findById(request.getAssigneeId())
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (!requester.getHouseholdId().equals(assignee.getHouseholdId())) {
            throw new CustomException(ChoreErrorCode.CHORE_4001);
        }

        Chore chore = Chore.builder()
                .assignee(assignee)
                .type(request.getChoreType())
                .date(request.getDate())
                .build();

        choreRepository.save(chore);

        return ChoreResponse.from(chore);
    }

    @Transactional
    public ChoreResponse updateChore(Long userId, Long choreId, UpdateChoreRequest request) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new CustomException(ChoreErrorCode.CHORE_1001));

        if (!requester.getHouseholdId().equals(chore.getAssignee().getHouseholdId())) {
            throw new CustomException(ChoreErrorCode.CHORE_4002);
        }

        if (chore.isCompleted()) {
            throw new CustomException(ChoreErrorCode.CHORE_1003);
        }

        User newAssignee = null;
        if (request.getAssigneeId() != null) {
            newAssignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));
            if (!requester.getHouseholdId().equals(newAssignee.getHouseholdId())) {
                throw new CustomException(ChoreErrorCode.CHORE_4001);
            }
        }

        chore.update(newAssignee, request.getDate());

        return ChoreResponse.from(chore);
    }

    @Transactional
    public void deleteChore(Long userId, Long choreId) {
        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new CustomException(ChoreErrorCode.CHORE_1001));

        if (!requester.getHouseholdId().equals(chore.getAssignee().getHouseholdId())) {
            throw new CustomException(ChoreErrorCode.CHORE_4002);
        }

        choreRepository.delete(chore);
    }

    @Transactional
    public CompleteChoreResponse completeChore(Long userId, Long choreId) {
        Chore chore = choreRepository.findById(choreId)
                .orElseThrow(() -> new CustomException(ChoreErrorCode.CHORE_1001));

        if (!chore.getAssignee().getId().equals(userId)) {
            throw new CustomException(ChoreErrorCode.CHORE_1002);
        }

        if (chore.isCompleted()) {
            throw new CustomException(ChoreErrorCode.CHORE_1003);
        }

        chore.complete();

        return CompleteChoreResponse.builder()
                .choreId(chore.getId())
                .isCompleted(true)
                .build();
    }
}
