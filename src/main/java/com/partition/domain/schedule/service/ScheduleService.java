package com.partition.domain.schedule.service;

import com.partition.domain.schedule.dto.request.ScheduleRequest;
import com.partition.domain.schedule.dto.request.ScheduleUpdateRequest;
import com.partition.domain.schedule.exception.ScheduleErrorCode;
import com.partition.domain.schedule.repository.ScheduleRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Schedule;
import com.partition.entity.User;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    // 일정 생성
    public Long createSchedule(Long userId, ScheduleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        // Request에 없는 title, type, time 등은 제외하고 content, date만 저장
        Schedule schedule = Schedule.builder()
                .user(user)
                .content(request.getContent())
                .date(request.getDate())
                .build();

        return scheduleRepository.save(schedule).getId();
    }

    // 일정 수정
    public void updateSchedule(Long userId, Long scheduleId, ScheduleUpdateRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // 작성자 본인 확인
        if (!schedule.getUser().getId().equals(userId)) {
            throw new CustomException(ScheduleErrorCode.NO_PERMISSION);
        }

        // 내용과 날짜만 업데이트 (값이 null이면 엔티티 내부에서 무시됨)
        schedule.update(request.getContent(), request.getDate(), null);
    }

    // 일정 삭제
    public void deleteSchedule(Long userId, Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        // 작성자 본인 확인
        if (!schedule.getUser().getId().equals(userId)) {
            throw new CustomException(ScheduleErrorCode.NO_PERMISSION);
        }

        scheduleRepository.delete(schedule);
    }
}