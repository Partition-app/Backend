package com.partition.domain.calender.service;

import com.partition.domain.calender.dto.response.CalendarDailyResponse;
import com.partition.domain.calender.dto.response.CalendarMonthlyResponse;
import com.partition.domain.chore.repository.ChoreRepository;
import com.partition.domain.schedule.repository.ScheduleRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.domain.utilitybill.repository.UtilityBillRepository;
import com.partition.entity.Chore;
import com.partition.entity.Schedule;
import com.partition.entity.User;
import com.partition.entity.UtilityBill;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ChoreRepository choreRepository;
    private final UtilityBillRepository utilityBillRepository;

    @Transactional(readOnly = true)
    public List<CalendarMonthlyResponse> getMonthlyCalendar(Long userId, int year, int month) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) return new ArrayList<>();
        Long householdId = user.getHouseholdId();

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Schedule> schedules = scheduleRepository.findAllByHouseholdIdAndDateRange(householdId, startDate, endDate);
        List<Chore> chores = choreRepository.findAllByHouseholdIdAndDateRange(householdId, startDate, endDate);
        List<UtilityBill> bills = utilityBillRepository.findAllByHouseholdIdAndDueDateBetween(householdId, startDate, endDate);

        Map<LocalDate, MonthlyCounts> countMap = new HashMap<>();

        for (Schedule s : schedules) countMap.computeIfAbsent(s.getDate(), k -> new MonthlyCounts()).scheduleCount++;
        for (Chore c : chores) countMap.computeIfAbsent(c.getDate(), k -> new MonthlyCounts()).choreCount++;
        for (UtilityBill b : bills) countMap.computeIfAbsent(b.getDueDate(), k -> new MonthlyCounts()).utilityBillsCount++;

        return countMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> CalendarMonthlyResponse.builder()
                        .date(entry.getKey())
                        .scheduleCount(entry.getValue().scheduleCount)
                        .choreCount(entry.getValue().choreCount)
                        .utilityBillsCount(entry.getValue().utilityBillsCount)
                        .build())
                .collect(Collectors.toList());
    }

    // 일간 상세 조회
    @Transactional(readOnly = true)
    public List<CalendarDailyResponse> getDailyCalendar(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            return new ArrayList<>();
        }
        Long householdId = user.getHouseholdId();

        // 1. 해당 날짜의 집안일(Chore) 조회 -> DTO 변환
        List<CalendarDailyResponse> chores = choreRepository.findAllByHouseholdIdAndDateRange(householdId, date, date)
                .stream()
                .map(chore -> CalendarDailyResponse.builder()
                        .category("CHORE")
                        .id(chore.getId())
                        .title(chore.getType().getDescription())
                        .assigneeName(chore.getAssignee().getName())
                        .isCompleted(chore.isCompleted())
                        .build())
                .toList();

        // 2. 해당 날짜의 일정(Schedule) 조회 -> DTO 변환
        List<CalendarDailyResponse> schedules = scheduleRepository.findAllByHouseholdIdAndDateRange(householdId, date, date)
                .stream()
                .map(schedule -> CalendarDailyResponse.builder()
                        .category("SCHEDULE")
                        .id(schedule.getId())
                        .title(schedule.getContent())
                        .assigneeName(schedule.getUser().getName())
                        .isCompleted(false)
                        .build())
                .toList();

        // 3. 두 리스트 합치기 (정렬: 카테고리순 CHORE -> SCHEDULE)
        return Stream.concat(chores.stream(), schedules.stream())
                .sorted(Comparator.comparing(CalendarDailyResponse::getCategory)) // CHORE가 먼저 오게 정렬 (알파벳순 C < S)
                .collect(Collectors.toList());
    }

    private static class MonthlyCounts {
        long scheduleCount = 0;
        long choreCount = 0;
        long utilityBillsCount = 0;
    }
}