package com.partition.domain.utilitybill.service;

import com.partition.domain.alarm.repository.AlarmRepository;
import com.partition.domain.alarm.service.FcmService;
import com.partition.domain.user.repository.UserRepository;
import com.partition.domain.utilitybill.repository.UtilityBillPaymentRepository;
import com.partition.domain.utilitybill.repository.UtilityBillRepository;
import com.partition.entity.Alarm;
import com.partition.entity.User;
import com.partition.entity.UtilityBill;
import com.partition.entity.UtilityBillPayment;
import com.partition.entity.enums.AlarmType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UtilityBillScheduler {

    private final UtilityBillRepository utilityBillRepository;
    private final UtilityBillPaymentRepository utilityBillPaymentRepository;
    private final UserRepository userRepository;
    private final AlarmRepository alarmRepository;
    private final FcmService fcmService;

    // 매월 1일 00:00 — 모든 bill에 대해 당월 payment 생성
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void createMonthlyPayments() {
        String yearMonth = YearMonth.now().toString();
        List<UtilityBill> bills = utilityBillRepository.findAll();

        for (UtilityBill bill : bills) {
            boolean exists = utilityBillPaymentRepository.findByBillAndYearMonth(bill, yearMonth).isPresent();
            if (exists) continue;

            Integer amount = bill.isFixed() ? bill.getAmount() : null;
            utilityBillPaymentRepository.save(
                    UtilityBillPayment.builder()
                            .bill(bill)
                            .yearMonth(yearMonth)
                            .amount(amount)
                            .build()
            );
        }

        log.info("[스케줄러] 월별 공과금 payment 생성 완료 - yearMonth: {}, 대상 bill 수: {}", yearMonth, bills.size());
    }

    // 변동 공과금 등록 시 createBill에서 즉시 발송으로 변경 — 스케줄러 비활성화
    // @Scheduled(cron = "0 0 9 * * *")
    // @Scheduled(cron = "0 0 9-23 * * *")
    @Transactional
    public void sendPayDayReminders() {
        int today = LocalDate.now().getDayOfMonth();
        List<UtilityBill> bills = utilityBillRepository.findAllByIsFixedFalseAndPayDay(today);

        for (UtilityBill bill : bills) {
            List<User> members = userRepository.findByHouseholdId(bill.getHousehold().getId());

            List<Alarm> alarms = members.stream()
                    .map(user -> Alarm.builder()
                            .userId(user.getId())
                            .type(AlarmType.BILL_PAYMENT_REMINDER)
                            .referenceId(bill.getId())
                            .build())
                    .toList();
            alarmRepository.saveAll(alarms);

            members.stream()
                    .filter(user -> user.getFcmToken() != null)
                    .forEach(user -> fcmService.sendPush(user.getFcmToken(), AlarmType.BILL_PAYMENT_REMINDER));
        }

        log.info("[스케줄러] 변동 공과금 납부일 알림 발송 완료 - 오늘 납부일 bill 수: {}", bills.size());
    }
}
