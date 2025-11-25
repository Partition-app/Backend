package com.partition.domain.calender.service;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final UserRepository userRepository;
    private final ScheduleRepository scheduleRepository;
    private final ChoreRepository choreRepository;
    private final UtilityBillRepository utilityBillRepository;

    @Transactional(readOnly = true)
    public List<CalendarMonthlyResponse> getMonthlyCalendar(Long userId, int year, int month) {
        // 1. 유저 및 그룹 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            return new ArrayList<>(); // 그룹이 없으면 빈 리스트 반환
        }
        Long householdId = user.getHouseholdId();

        // 2. 조회 기간 설정 (해당 월의 1일 ~ 마지막 날)
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 3. 데이터 조회
        List<Schedule> schedules = scheduleRepository.findAllByHouseholdIdAndDateRange(householdId, startDate, endDate);
        List<Chore> chores = choreRepository.findAllByHouseholdIdAndDateRange(householdId, startDate, endDate);
        List<UtilityBill> bills = utilityBillRepository.findAllByHouseholdIdAndDueDateBetween(householdId, startDate, endDate);

        // 4. 날짜별로 데이터 집계 (Map<LocalDate, Counts>)
        Map<LocalDate, MonthlyCounts> countMap = new HashMap<>();

        // Schedule 카운트
        for (Schedule s : schedules) {
            countMap.computeIfAbsent(s.getDate(), k -> new MonthlyCounts()).scheduleCount++;
        }
        // Chore 카운트
        for (Chore c : chores) {
            countMap.computeIfAbsent(c.getDate(), k -> new MonthlyCounts()).choreCount++;
        }
        // UtilityBill 카운트
        for (UtilityBill b : bills) {
            countMap.computeIfAbsent(b.getDueDate(), k -> new MonthlyCounts()).utilityBillsCount++;
        }

        // 5. DTO 변환 (데이터가 있는 날짜만 리스트에 담음)
        return countMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // 날짜순 정렬
                .map(entry -> CalendarMonthlyResponse.builder()
                        .date(entry.getKey())
                        .scheduleCount(entry.getValue().scheduleCount)
                        .choreCount(entry.getValue().choreCount)
                        .utilityBillsCount(entry.getValue().utilityBillsCount)
                        .build())
                .collect(Collectors.toList());
    }

    // 카운트 집계를 위한 내부 클래스
    private static class MonthlyCounts {
        long scheduleCount = 0;
        long choreCount = 0;
        long utilityBillsCount = 0;
    }
}