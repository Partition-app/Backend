package com.partition.domain.calender.service;

import com.partition.domain.calender.dto.response.CalendarDailyResponse;
import com.partition.domain.calender.dto.response.CalendarMonthlyResponse;
import com.partition.domain.chore.repository.ChoreRepository;
import com.partition.domain.schedule.repository.ScheduleRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.domain.utilitybill.repository.UtilityBillPaymentRepository;
import com.partition.domain.utilitybill.repository.UtilityBillRepository;
import com.partition.entity.Chore;
import com.partition.entity.Schedule;
import com.partition.entity.User;
import com.partition.entity.UtilityBill;
import com.partition.entity.UtilityBillPayment;
import com.partition.entity.enums.BillStatus;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
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
    private final UtilityBillPaymentRepository utilityBillPaymentRepository;

    // 월간 캘린더 조회
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
        List<UtilityBill> bills = utilityBillRepository.findAllByHouseholdIdOrderByIdAsc(householdId);

        Map<LocalDate, MonthlyCounts> countMap = new HashMap<>();

        for (Schedule s : schedules) countMap.computeIfAbsent(s.getDate(), k -> new MonthlyCounts()).scheduleCount++;
        for (Chore c : chores) countMap.computeIfAbsent(c.getDate(), k -> new MonthlyCounts()).choreCount++;
        for (UtilityBill b : bills) {
            int actualDay = Math.min(b.getPayDay(), endDate.lengthOfMonth());
            countMap.computeIfAbsent(LocalDate.of(year, month, actualDay), k -> new MonthlyCounts()).utilityBillsCount++;
        }

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

    // 일간 상세 조회 (집안일 + 일정 통합 리스트)
    @Transactional(readOnly = true)
    public List<CalendarDailyResponse> getDailyCalendar(Long userId, LocalDate date) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            return new ArrayList<>();
        }
        Long householdId = user.getHouseholdId();

        // 해당 날짜의 집안일(Chore) 조회 -> DTO 변환
        List<CalendarDailyResponse> chores = choreRepository.findAllByHouseholdIdAndDateRange(householdId, date, date)
                .stream()
                .map(chore -> CalendarDailyResponse.builder()
                        .category("CHORE")
                        .id(chore.getId())
                        .title(chore.getType().getDescription())
                        .assigneeName(
                                Optional.ofNullable(chore.getAssignee())
                                        .map(User::getName)
                                        .orElse(null)
                        )
                        .isCompleted(chore.isCompleted())
                        .build())
                .toList();

        // 해당 날짜의 일정(Schedule) 조회 -> DTO 변환
        List<CalendarDailyResponse> schedules = scheduleRepository.findAllByHouseholdIdAndDateRange(householdId, date, date)
                .stream()
                .map(schedule -> CalendarDailyResponse.builder()
                        .category("SCHEDULE")
                        .id(schedule.getId())
                        .title(schedule.getContent())
                        .assigneeName(schedule.getUser().getName())
                        .isCompleted(false)
                        .isOwner(Objects.equals(schedule.getUser().getId(), userId))
                        .build())
                .toList();

        // 해당 날짜의 공과금 조회 (payDay 기준, 월말 처리 포함)
        int daysInMonth = date.lengthOfMonth();
        String yearMonth = YearMonth.from(date).toString();
        List<UtilityBill> todayBills = utilityBillRepository.findAllByHouseholdIdOrderByIdAsc(householdId)
                .stream()
                .filter(b -> Math.min(b.getPayDay(), daysInMonth) == date.getDayOfMonth())
                .toList();

        Map<Long, UtilityBillPayment> paymentMap = new HashMap<>();
        utilityBillPaymentRepository.findAllByBillInAndYearMonth(todayBills, yearMonth)
                .forEach(p -> paymentMap.put(p.getBill().getId(), p));

        List<CalendarDailyResponse> bills = todayBills.stream()
                .map(b -> {
                    UtilityBillPayment p = paymentMap.get(b.getId());
                    return CalendarDailyResponse.builder()
                            .category("UTILITY_BILL")
                            .id(b.getId())
                            .title(b.getBillType().getLabel())
                            .amount(p != null ? p.getAmount() : b.getAmount())
                            .isCompleted(p != null && p.getStatus() == BillStatus.SETTLED)
                            .build();
                })
                .toList();

        return Stream.concat(Stream.concat(chores.stream(), schedules.stream()), bills.stream())
                .sorted(Comparator.comparing(CalendarDailyResponse::getCategory))
                .collect(Collectors.toList());
    }

    // 내부 클래스: 날짜별 개수 집계용
    private static class MonthlyCounts {
        long scheduleCount = 0;
        long choreCount = 0;
        long utilityBillsCount = 0;
    }
}