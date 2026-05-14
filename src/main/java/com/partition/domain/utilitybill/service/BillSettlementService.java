package com.partition.domain.utilitybill.service;

import com.partition.domain.alarm.service.AlarmService;
import com.partition.domain.supply.repository.SettlementMemberRepository;
import com.partition.domain.supply.repository.SettlementRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.domain.utilitybill.dto.request.CreateBillSettlementRequest;
import com.partition.domain.utilitybill.dto.response.BillSettlementDetailResponse;
import com.partition.domain.utilitybill.dto.response.BillSettlementRequestedListResponse;
import com.partition.domain.utilitybill.dto.response.ConfirmBillSettlementResponse;
import com.partition.domain.utilitybill.dto.response.CreateBillSettlementResponse;
import com.partition.domain.utilitybill.exception.BillErrorCode;
import com.partition.domain.utilitybill.repository.UtilityBillPaymentRepository;
import com.partition.domain.utilitybill.repository.UtilityBillRepository;
import com.partition.entity.Settlement;
import com.partition.entity.SettlementMember;
import com.partition.entity.User;
import com.partition.entity.UtilityBill;
import com.partition.entity.UtilityBillPayment;
import com.partition.entity.enums.AlarmType;
import com.partition.entity.enums.BillStatus;

import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BillSettlementService {

    private final UserRepository userRepository;
    private final UtilityBillRepository utilityBillRepository;
    private final UtilityBillPaymentRepository utilityBillPaymentRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementMemberRepository settlementMemberRepository;
    private final AlarmService alarmService;

    @Transactional
    public CreateBillSettlementResponse createSettlement(Long userId, CreateBillSettlementRequest request) {
        if (request.getPaymentIds() == null || request.getPaymentIds().isEmpty()) {
            throw new CustomException(BillErrorCode.BILL_3001);
        }

        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            throw new CustomException(BillErrorCode.BILL_3006);
        }

        User requestUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));
        if (requestUser.getHouseholdId() == null) {
            throw new CustomException(BillErrorCode.BILL_9002);
        }
        Long householdId = requestUser.getHouseholdId();

        List<UtilityBillPayment> payments = utilityBillPaymentRepository
                .findAllByIdInAndHouseholdId(request.getPaymentIds(), householdId);
        if (payments.size() != request.getPaymentIds().size()) {
            throw new CustomException(BillErrorCode.BILL_3002);
        }

        if (payments.stream().anyMatch(p -> p.getStatus() == BillStatus.REQUESTED)) {
            throw new CustomException(BillErrorCode.BILL_3003);
        }
        if (payments.stream().anyMatch(p -> p.getStatus() == BillStatus.SETTLED)) {
            throw new CustomException(BillErrorCode.BILL_3004);
        }

        List<User> members = userRepository.findAllById(request.getMemberIds());
        Set<Long> householdMemberIds = userRepository.findByHouseholdId(householdId)
                .stream().map(User::getId).collect(Collectors.toSet());
        if (members.size() != request.getMemberIds().size() ||
                members.stream().anyMatch(m -> !householdMemberIds.contains(m.getId()))) {
            throw new CustomException(BillErrorCode.BILL_3005);
        }

        int totalAmount = payments.stream().mapToInt(UtilityBillPayment::getAmount).sum();
        int memberCount = members.size();
        int amountPerMember = memberCount > 0 ? totalAmount / memberCount : 0;
        int remainder = memberCount > 0 ? totalAmount % memberCount : 0;

        Settlement settlement = settlementRepository.save(
                Settlement.builder()
                        .household(payments.get(0).getBill().getHousehold())
                        .totalAmount(totalAmount)
                        .memberCount(memberCount)
                        .amountPerMember(amountPerMember)
                        .build()
        );

        List<SettlementMember> savedMembers = new ArrayList<>();
        for (int i = 0; i < members.size(); i++) {
            int memberAmount = amountPerMember + (i < remainder ? 1 : 0);
            savedMembers.add(settlementMemberRepository.save(
                    SettlementMember.builder()
                            .settlement(settlement)
                            .user(members.get(i))
                            .amount(memberAmount)
                            .build()
            ));
        }

        // payment 상태 업데이트 + 기존 bill 상태도 동기화 (confirmSettlement 호환)
        payments.forEach(p -> {
            p.requestSettlement(settlement);
            p.getBill().requestSettlement(settlement);
        });

        alarmService.createSettlementAlarms(savedMembers, settlement.getId(), AlarmType.BILL_SETTLEMENT_REQUESTED);

        return CreateBillSettlementResponse.builder()
                .settlementId(settlement.getId())
                .totalAmount(totalAmount)
                .memberCount(memberCount)
                .amountPerMember(amountPerMember)
                .settledAt(settlement.getCreatedAt())
                .members(savedMembers.stream()
                        .map(sm -> CreateBillSettlementResponse.MemberResponse.builder()
                                .userId(sm.getUser().getId())
                                .name(sm.getUser().getName())
                                .amount(sm.getAmount())
                                .build())
                        .toList())
                .items(payments.stream()
                        .map(p -> CreateBillSettlementResponse.BillItemResponse.builder()
                                .paymentId(p.getId())
                                .billId(p.getBill().getId())
                                .utilityTypeName(p.getBill().getBillType().getLabel())
                                .amount(p.getAmount())
                                .build())
                        .toList())
                .build();
    }

    @Transactional
    public ConfirmBillSettlementResponse confirmSettlement(Long userId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_4001));

        User requestUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));
        if (!settlement.getHousehold().getId().equals(requestUser.getHouseholdId())) {
            throw new CustomException(BillErrorCode.BILL_4004);
        }

        if (Boolean.TRUE.equals(settlement.getIsConfirmed())) {
            throw new CustomException(BillErrorCode.BILL_4002);
        }

        List<UtilityBill> bills = utilityBillRepository.findAllBySettlementId(settlementId);
        boolean hasNonRequested = bills.stream().anyMatch(b -> b.getStatus() != BillStatus.REQUESTED);
        if (hasNonRequested) {
            throw new CustomException(BillErrorCode.BILL_4003);
        }

        settlement.confirm();
        bills.forEach(bill -> bill.settle(settlement));

        List<SettlementMember> members = settlementMemberRepository.findAllBySettlementId(settlementId);

        alarmService.createSettlementAlarms(members, settlement.getId(), AlarmType.BILL_SETTLEMENT_CONFIRMED);

        return ConfirmBillSettlementResponse.builder()
                .settlementId(settlement.getId())
                .totalAmount(settlement.getTotalAmount())
                .memberCount(settlement.getMemberCount())
                .amountPerMember(settlement.getAmountPerMember())
                .confirmedAt(settlement.getConfirmedAt())
                .members(members.stream()
                        .map(sm -> ConfirmBillSettlementResponse.MemberResponse.builder()
                                .userId(sm.getUser().getId())
                                .name(sm.getUser().getName())
                                .amount(sm.getAmount())
                                .build())
                        .toList())
                .items(bills.stream()
                        .map(b -> ConfirmBillSettlementResponse.BillItemResponse.builder()
                                .billId(b.getId())
                                .utilityTypeName(b.getBillType().getLabel())
                                .amount(b.getAmount())
                                .build())
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public BillSettlementRequestedListResponse getRequestedSettlements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));
        if (user.getHouseholdId() == null) {
            throw new CustomException(BillErrorCode.BILL_9002);
        }

        List<BillSettlementRequestedListResponse.SettlementItem> settlements =
                utilityBillRepository.findAllByHouseholdIdAndStatus(user.getHouseholdId(), BillStatus.REQUESTED)
                        .stream()
                        .collect(Collectors.groupingBy(b -> b.getSettlement().getId()))
                        .values().stream()
                        .map(bills -> {
                            Settlement s = bills.get(0).getSettlement();
                            return BillSettlementRequestedListResponse.SettlementItem.builder()
                                    .settlementId(s.getId())
                                    .totalAmount(s.getTotalAmount())
                                    .amountPerMember(s.getAmountPerMember())
                                    .memberCount(s.getMemberCount())
                                    .requestedAt(s.getCreatedAt())
                                    .build();
                        })
                        .toList();

        return BillSettlementRequestedListResponse.builder()
                .totalCount(settlements.size())
                .settlements(settlements)
                .build();
    }

    @Transactional(readOnly = true)
    public BillSettlementDetailResponse getSettlementDetail(Long userId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_4001));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(BillErrorCode.BILL_9001));
        if (!settlement.getHousehold().getId().equals(user.getHouseholdId())) {
            throw new CustomException(BillErrorCode.BILL_4004);
        }

        List<SettlementMember> members = settlementMemberRepository.findAllBySettlementId(settlementId);
        List<UtilityBill> bills = utilityBillRepository.findAllBySettlementId(settlementId);

        return BillSettlementDetailResponse.builder()
                .settlementId(settlement.getId())
                .totalAmount(settlement.getTotalAmount())
                .amountPerMember(settlement.getAmountPerMember())
                .memberCount(settlement.getMemberCount())
                .isConfirmed(settlement.getIsConfirmed())
                .confirmedAt(settlement.getConfirmedAt())
                .members(members.stream()
                        .map(sm -> BillSettlementDetailResponse.MemberItem.builder()
                                .userId(sm.getUser().getId())
                                .name(sm.getUser().getName())
                                .amount(sm.getAmount())
                                .build())
                        .toList())
                .bills(bills.stream()
                        .map(b -> BillSettlementDetailResponse.BillItem.builder()
                                .billId(b.getId())
                                .utilityTypeName(b.getBillType().getLabel())
                                .payDay(b.getPayDay())
                                .amount(b.getAmount())
                                .build())
                        .toList())
                .build();
    }
}
