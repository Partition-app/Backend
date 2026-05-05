package com.partition.domain.chore.service;

import com.partition.domain.chore.dto.response.CompleteChoreResponse;
import com.partition.domain.chore.exception.ChoreErrorCode;
import com.partition.domain.chore.repository.ChoreRepository;
import com.partition.entity.Chore;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChoreService {

    private final ChoreRepository choreRepository;

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
