package com.partition.domain.utilitybill.service;

import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.utilitybill.dto.request.CreateBillRequest;
import com.partition.domain.utilitybill.dto.response.BillResponse;
import com.partition.domain.utilitybill.dto.response.CreateBillResponse;
import com.partition.domain.utilitybill.exception.BillErrorCode;
import com.partition.domain.utilitybill.repository.UtilityBillRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Household;
import com.partition.entity.UtilityBill;
import com.partition.entity.User;
import com.partition.entity.enums.BillCategoryType;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UtilityBillService {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final UtilityBillRepository utilityBillRepository;

    @Transactional
    public CreateBillResponse createBill(Long userId, CreateBillRequest request) {
        validateRequest(request);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (user.getHouseholdId() == null) {
            throw new CustomException(BillErrorCode.BILL_9002);
        }

        Household household = householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9002));

        BillCategoryType billType = parseBillType(request.getUtilityType());
        LocalDate dueDate = parseDate(request.getDueDate());

        UtilityBill bill = utilityBillRepository.save(
                UtilityBill.builder()
                        .household(household)
                        .billType(billType)
                        .dueDate(dueDate)
                        .amount(request.getAmount())
                        .note(request.getNote())
                        .build()
        );

        return CreateBillResponse.builder()
                .billId(bill.getId())
                .billType(bill.getBillType().name())
                .billTypeName(bill.getBillType().getLabel())
                .dueDate(bill.getDueDate())
                .amount(bill.getAmount())
                .note(bill.getNote())
                .createdAt(bill.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<BillResponse> getBills(Long userId, String startDate, String endDate) {
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new CustomException(BillErrorCode.BILL_2001);
        }
        if (endDate == null || endDate.trim().isEmpty()) {
            throw new CustomException(BillErrorCode.BILL_2002);
        }

        LocalDate start = parseDateForQuery(startDate);
        LocalDate end = parseDateForQuery(endDate);

        if (start.isAfter(end)) {
            throw new CustomException(BillErrorCode.BILL_2004);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));

        if (user.getHouseholdId() == null) {
            throw new CustomException(BillErrorCode.BILL_2005);
        }

        return utilityBillRepository.findAllByHouseholdIdAndDueDateBetween(user.getHouseholdId(), start, end)
                .stream()
                .map(BillResponse::from)
                .toList();
    }

    private LocalDate parseDateForQuery(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new CustomException(BillErrorCode.BILL_2003);
        }
    }

    private void validateRequest(CreateBillRequest request) {
        if (request.getUtilityType() == null || request.getUtilityType().trim().isEmpty()) {
            throw new CustomException(BillErrorCode.BILL_1001);
        }

        parseBillType(request.getUtilityType());

        if (request.getDueDate() == null || request.getDueDate().trim().isEmpty()) {
            throw new CustomException(BillErrorCode.BILL_1003);
        }

        parseDate(request.getDueDate());

        if (request.getAmount() == null) {
            throw new CustomException(BillErrorCode.BILL_1005);
        }

        if (request.getAmount() < 0) {
            throw new CustomException(BillErrorCode.BILL_1006);
        }
    }

    private BillCategoryType parseBillType(String utilityType) {
        try {
            return BillCategoryType.valueOf(utilityType);
        } catch (Exception e) {
            throw new CustomException(BillErrorCode.BILL_1002);
        }
    }

    private LocalDate parseDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new CustomException(BillErrorCode.BILL_1004);
        }
    }
}
