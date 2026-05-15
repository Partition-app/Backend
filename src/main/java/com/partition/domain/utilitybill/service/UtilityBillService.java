package com.partition.domain.utilitybill.service;

import com.partition.domain.alarm.repository.AlarmRepository;
import com.partition.domain.alarm.service.FcmService;
import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.utilitybill.dto.request.CreateBillRequest;
import com.partition.domain.utilitybill.dto.request.UpdateBillRequest;
import com.partition.domain.utilitybill.dto.request.UpdatePaymentAmountRequest;
import com.partition.domain.utilitybill.dto.response.BillPaymentListResponse;
import com.partition.domain.utilitybill.dto.response.BillResponse;
import com.partition.domain.utilitybill.dto.response.BillSettlementListResponse;
import com.partition.domain.utilitybill.dto.response.CreateBillResponse;
import com.partition.domain.utilitybill.dto.response.ToggleBillSettlementStatusResponse;
import com.partition.domain.utilitybill.dto.response.UpdateBillResponse;
import com.partition.domain.utilitybill.dto.response.UpdatePaymentAmountResponse;
import com.partition.domain.utilitybill.exception.BillErrorCode;
import com.partition.domain.utilitybill.repository.UtilityBillPaymentRepository;
import com.partition.domain.utilitybill.repository.UtilityBillRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Alarm;
import com.partition.entity.Household;
import com.partition.entity.UtilityBill;
import com.partition.entity.UtilityBillPayment;
import com.partition.entity.User;
import com.partition.entity.enums.AlarmType;
import com.partition.entity.enums.BillCategoryType;
import com.partition.entity.enums.BillStatus;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UtilityBillService {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final UtilityBillRepository utilityBillRepository;
    private final UtilityBillPaymentRepository utilityBillPaymentRepository;
    private final AlarmRepository alarmRepository;
    private final FcmService fcmService;

    @Transactional
    public CreateBillResponse createBill(Long userId, CreateBillRequest request) {
        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (user.getHouseholdId() == null) {
            throw new CustomException(BillErrorCode.BILL_1008);
        }

        Household household = householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9002));

        BillCategoryType billType = parseBillType(request.getUtilityType());
        boolean isFixed = request.getIsFixed();
        Integer fixedAmount = isFixed ? request.getAmount() : null;

        UtilityBill bill = utilityBillRepository.save(
                UtilityBill.builder()
                        .household(household)
                        .billType(billType)
                        .payDay(request.getPayDay())
                        .isFixed(isFixed)
                        .amount(fixedAmount)
                        .note(request.getNote())
                        .build()
        );

        Integer thisMonthAmount = isFixed ? fixedAmount : request.getAmount();
        UtilityBillPayment payment = utilityBillPaymentRepository.save(
                UtilityBillPayment.builder()
                        .bill(bill)
                        .yearMonth(YearMonth.now().toString())
                        .amount(thisMonthAmount)
                        .build()
        );

        if (!isFixed) {
            List<User> members = userRepository.findByHouseholdId(household.getId());
            List<Alarm> alarms = members.stream()
                    .map(u -> Alarm.builder()
                            .userId(u.getId())
                            .type(AlarmType.BILL_PAYMENT_REMINDER)
                            .referenceId(bill.getId())
                            .build())
                    .toList();
            alarmRepository.saveAll(alarms);
            members.stream()
                    .filter(u -> u.getFcmToken() != null)
                    .forEach(u -> fcmService.sendPush(u.getFcmToken(), AlarmType.BILL_PAYMENT_REMINDER));
        }

        return CreateBillResponse.builder()
                .billId(bill.getId())
                .utilityType(bill.getBillType().name())
                .utilityTypeName(bill.getBillType().getLabel())
                .payDay(bill.getPayDay())
                .isFixed(bill.isFixed())
                .amount(bill.getAmount())
                .thisMonthAmount(payment.getAmount())
                .note(bill.getNote())
                .status(payment.getStatus().name())
                .createdAt(bill.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getBills(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (user.getHouseholdId() == null) {
            throw new CustomException(BillErrorCode.BILL_2005);
        }

        List<UtilityBill> bills = utilityBillRepository.findAllByHouseholdIdOrderByIdAsc(user.getHouseholdId());
        String yearMonth = YearMonth.now().toString();

        Map<Long, UtilityBillPayment> paymentByBillId = new java.util.HashMap<>();
        utilityBillPaymentRepository.findAllByBillInAndYearMonth(bills, yearMonth)
                .forEach(p -> paymentByBillId.put(p.getBill().getId(), p));

        return bills.stream()
                .map(b -> BillResponse.from(b, paymentByBillId.get(b.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public BillSettlementListResponse getSettlementBills(Long userId, String startDate, String endDate) {
        if (startDate == null || startDate.isBlank()) throw new CustomException(BillErrorCode.BILL_2001);
        if (endDate == null || endDate.isBlank()) throw new CustomException(BillErrorCode.BILL_2002);

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            throw new CustomException(BillErrorCode.BILL_2003);
        }

        if (end.isBefore(start)) throw new CustomException(BillErrorCode.BILL_2004);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (user.getHouseholdId() == null) {
            throw new CustomException(BillErrorCode.BILL_2005);
        }

        int memberCount = userRepository.findByHouseholdId(user.getHouseholdId()).size();

        List<BillSettlementListResponse.BillItem> billItems = utilityBillPaymentRepository
                .findAllByHouseholdIdAndStatus(user.getHouseholdId(), BillStatus.UNSETTLED)
                .stream()
                .filter(p -> p.getAmount() != null)
                .filter(p -> {
                    YearMonth ym = YearMonth.parse(p.getYearMonth());
                    int day = Math.min(p.getBill().getPayDay(), ym.lengthOfMonth());
                    LocalDate dueDate = ym.atDay(day);
                    return !dueDate.isBefore(start) && !dueDate.isAfter(end);
                })
                .map(p -> {
                    YearMonth ym = YearMonth.parse(p.getYearMonth());
                    int day = Math.min(p.getBill().getPayDay(), ym.lengthOfMonth());
                    String dueDate = ym.atDay(day).toString();
                    return BillSettlementListResponse.BillItem.builder()
                            .paymentId(p.getId())
                            .billId(p.getBill().getId())
                            .utilityTypeName(p.getBill().getBillType().getLabel())
                            .dueDate(dueDate)
                            .amount(p.getAmount())
                            .note(p.getBill().getNote())
                            .build();
                })
                .toList();

        int totalAmount = billItems.stream().mapToInt(BillSettlementListResponse.BillItem::getAmount).sum();
        int amountPerMember = memberCount > 0 ? totalAmount / memberCount : 0;
        int remainder = memberCount > 0 ? totalAmount % memberCount : 0;

        return BillSettlementListResponse.builder()
                .totalCount(billItems.size())
                .totalAmount(totalAmount)
                .memberCount(memberCount)
                .amountPerMember(amountPerMember)
                .remainder(remainder)
                .bills(billItems)
                .build();
    }

    @Transactional
    public UpdateBillResponse updateBill(Long userId, Long billId, UpdateBillRequest request) {
        UtilityBill bill = utilityBillRepository.findById(billId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_6008));

        User caller = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (caller.getHouseholdId() == null ||
                !caller.getHouseholdId().equals(bill.getHousehold().getId())) {
            throw new CustomException(BillErrorCode.BILL_6009);
        }

        List<UtilityBillPayment> payments = utilityBillPaymentRepository.findAllByBill(bill);
        if (payments.stream().anyMatch(p -> p.getStatus() == BillStatus.REQUESTED)) {
            throw new CustomException(BillErrorCode.BILL_6007);
        }

        validateUpdateRequest(request);

        BillCategoryType billType = parseBillType6(request.getUtilityType());
        boolean isFixed = request.getIsFixed();
        Integer fixedAmount = isFixed ? request.getAmount() : null;
        bill.update(billType, request.getPayDay(), isFixed, fixedAmount, request.getNote());

        return UpdateBillResponse.builder()
                .billId(bill.getId())
                .utilityType(bill.getBillType().name())
                .utilityTypeName(bill.getBillType().getLabel())
                .payDay(bill.getPayDay())
                .isFixed(bill.isFixed())
                .amount(bill.getAmount())
                .note(bill.getNote())
                .build();
    }

    @Transactional
    public void deleteBill(Long userId, Long billId) {
        UtilityBill bill = utilityBillRepository.findById(billId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_7001));

        User caller = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (caller.getHouseholdId() == null ||
                !caller.getHouseholdId().equals(bill.getHousehold().getId())) {
            throw new CustomException(BillErrorCode.BILL_7003);
        }

        List<UtilityBillPayment> payments = utilityBillPaymentRepository.findAllByBill(bill);

        if (payments.stream().anyMatch(p -> p.getStatus() == BillStatus.SETTLED)) {
            throw new CustomException(BillErrorCode.BILL_7002);
        }
        if (payments.stream().anyMatch(p -> p.getStatus() == BillStatus.REQUESTED)) {
            throw new CustomException(BillErrorCode.BILL_7004);
        }

        utilityBillPaymentRepository.deleteAllByBill(bill);
        utilityBillRepository.delete(bill);
    }

    @Transactional(readOnly = true)
    public BillPaymentListResponse getPayments(Long userId, String startDate, String endDate) {
        if (startDate == null || startDate.isBlank()) throw new CustomException(BillErrorCode.BILL_2001);
        if (endDate == null || endDate.isBlank()) throw new CustomException(BillErrorCode.BILL_2002);

        LocalDate start;
        LocalDate end;
        try {
            start = LocalDate.parse(startDate);
            end = LocalDate.parse(endDate);
        } catch (DateTimeParseException e) {
            throw new CustomException(BillErrorCode.BILL_2003);
        }

        if (end.isBefore(start)) throw new CustomException(BillErrorCode.BILL_2004);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (user.getHouseholdId() == null) throw new CustomException(BillErrorCode.BILL_2005);

        String startYm = YearMonth.from(start).toString();
        String endYm = YearMonth.from(end).toString();

        List<BillPaymentListResponse.PaymentItem> items = utilityBillPaymentRepository
                .findAllByHouseholdIdAndYearMonthBetween(user.getHouseholdId(), startYm, endYm)
                .stream()
                .filter(p -> {
                    YearMonth ym = YearMonth.parse(p.getYearMonth());
                    int day = Math.min(p.getBill().getPayDay(), ym.lengthOfMonth());
                    LocalDate dueDate = ym.atDay(day);
                    return !dueDate.isBefore(start) && !dueDate.isAfter(end);
                })
                .map(p -> {
                    YearMonth ym = YearMonth.parse(p.getYearMonth());
                    int day = Math.min(p.getBill().getPayDay(), ym.lengthOfMonth());
                    return BillPaymentListResponse.PaymentItem.builder()
                            .paymentId(p.getId())
                            .billId(p.getBill().getId())
                            .utilityType(p.getBill().getBillType().name())
                            .utilityTypeName(p.getBill().getBillType().getLabel())
                            .isFixed(p.getBill().isFixed())
                            .payDay(p.getBill().getPayDay())
                            .dueDate(ym.atDay(day).toString())
                            .amount(p.getAmount())
                            .status(p.getStatus().name())
                            .build();
                })
                .toList();

        return BillPaymentListResponse.builder()
                .totalCount(items.size())
                .payments(items)
                .build();
    }

    @Transactional
    public ToggleBillSettlementStatusResponse togglePaymentSettlementStatus(Long userId, Long paymentId) {
        UtilityBillPayment payment = utilityBillPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_5001));

        User caller = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (caller.getHouseholdId() == null ||
                !caller.getHouseholdId().equals(payment.getBill().getHousehold().getId())) {
            throw new CustomException(BillErrorCode.BILL_5003);
        }

        if (payment.getStatus() == BillStatus.REQUESTED) {
            throw new CustomException(BillErrorCode.BILL_5002);
        }

        payment.toggleSettlementStatus();

        return ToggleBillSettlementStatusResponse.builder()
                .paymentId(payment.getId())
                .billId(payment.getBill().getId())
                .utilityType(payment.getBill().getBillType().name())
                .utilityTypeName(payment.getBill().getBillType().getLabel())
                .status(payment.getStatus().name())
                .build();
    }

    @Transactional
    public UpdatePaymentAmountResponse updatePaymentAmount(Long userId, Long billId, String yearMonth, UpdatePaymentAmountRequest request) {
        try {
            YearMonth.parse(yearMonth);
        } catch (DateTimeParseException e) {
            throw new CustomException(BillErrorCode.BILL_8004);
        }

        if (request.getThisMonthAmount() == null) {
            throw new CustomException(BillErrorCode.BILL_8005);
        }
        if (request.getThisMonthAmount() < 1) {
            throw new CustomException(BillErrorCode.BILL_8006);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (user.getHouseholdId() == null) {
            throw new CustomException(BillErrorCode.BILL_8007);
        }

        UtilityBill bill = utilityBillRepository.findById(billId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_8001));

        if (!user.getHouseholdId().equals(bill.getHousehold().getId())) {
            throw new CustomException(BillErrorCode.BILL_8007);
        }

        if (bill.isFixed()) {
            throw new CustomException(BillErrorCode.BILL_8002);
        }

        UtilityBillPayment payment = utilityBillPaymentRepository.findByBillAndYearMonth(bill, yearMonth)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_8003));

        payment.updateAmount(request.getThisMonthAmount());

        return UpdatePaymentAmountResponse.builder()
                .billId(bill.getId())
                .yearMonth(yearMonth)
                .thisMonthAmount(payment.getAmount())
                .build();
    }

    private void validateRequest(CreateBillRequest request) {
        if (request.getUtilityType() == null || request.getUtilityType().trim().isEmpty()) {
            throw new CustomException(BillErrorCode.BILL_1001);
        }

        parseBillType(request.getUtilityType());

        if (request.getPayDay() == null) {
            throw new CustomException(BillErrorCode.BILL_1003);
        }

        if (request.getPayDay() < 1 || request.getPayDay() > 31) {
            throw new CustomException(BillErrorCode.BILL_1007);
        }

        if (request.getIsFixed() == null) {
            throw new CustomException(BillErrorCode.BILL_1009);
        }

        if (Boolean.TRUE.equals(request.getIsFixed())) {
            if (request.getAmount() == null || request.getAmount() < 1) {
                throw new CustomException(BillErrorCode.BILL_1006);
            }
        } else if (request.getAmount() != null && request.getAmount() < 1) {
            throw new CustomException(BillErrorCode.BILL_1006);
        }
    }

    private void validateUpdateRequest(UpdateBillRequest request) {
        if (request.getUtilityType() == null || request.getUtilityType().trim().isEmpty()) {
            throw new CustomException(BillErrorCode.BILL_6001);
        }
        if (request.getPayDay() == null) {
            throw new CustomException(BillErrorCode.BILL_6003);
        }
        if (request.getPayDay() < 1 || request.getPayDay() > 31) {
            throw new CustomException(BillErrorCode.BILL_6004);
        }
        if (request.getIsFixed() == null) {
            throw new CustomException(BillErrorCode.BILL_6010);
        }
        if (Boolean.TRUE.equals(request.getIsFixed())) {
            if (request.getAmount() == null || request.getAmount() < 1) {
                throw new CustomException(BillErrorCode.BILL_6006);
            }
        } else if (request.getAmount() != null && request.getAmount() < 1) {
            throw new CustomException(BillErrorCode.BILL_6006);
        }
    }

    private BillCategoryType parseBillType(String utilityType) {
        try {
            return BillCategoryType.valueOf(utilityType);
        } catch (Exception e) {
            throw new CustomException(BillErrorCode.BILL_1002);
        }
    }

    private BillCategoryType parseBillType6(String utilityType) {
        try {
            return BillCategoryType.valueOf(utilityType);
        } catch (Exception e) {
            throw new CustomException(BillErrorCode.BILL_6002);
        }
    }
}
