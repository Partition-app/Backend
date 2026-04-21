package com.partition.domain.utilitybill.service;

import com.partition.domain.supply.repository.SettlementMemberRepository;
import com.partition.domain.supply.repository.SettlementRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.domain.utilitybill.dto.request.CreateBillSettlementRequest;
import com.partition.domain.utilitybill.dto.response.ConfirmBillSettlementResponse;
import com.partition.domain.utilitybill.dto.response.CreateBillSettlementResponse;
import com.partition.domain.utilitybill.exception.BillErrorCode;
import com.partition.domain.utilitybill.repository.UtilityBillRepository;
import com.partition.entity.Settlement;
import com.partition.entity.SettlementMember;
import com.partition.entity.User;
import com.partition.entity.UtilityBill;
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
    private final SettlementRepository settlementRepository;
    private final SettlementMemberRepository settlementMemberRepository;

    @Transactional
    public CreateBillSettlementResponse createSettlement(Long userId, CreateBillSettlementRequest request) {
        if (request.getBillIds() == null || request.getBillIds().isEmpty()) {
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

        List<UtilityBill> bills = utilityBillRepository.findAllById(request.getBillIds());
        if (bills.size() != request.getBillIds().size()) {
            throw new CustomException(BillErrorCode.BILL_3002);
        }

        boolean hasRequested = bills.stream().anyMatch(b -> b.getStatus() == BillStatus.REQUESTED);
        if (hasRequested) {
            throw new CustomException(BillErrorCode.BILL_3003);
        }

        boolean hasSettled = bills.stream().anyMatch(b -> b.getStatus() == BillStatus.SETTLED);
        if (hasSettled) {
            throw new CustomException(BillErrorCode.BILL_3004);
        }

        List<User> members = userRepository.findAllById(request.getMemberIds());
        Set<Long> householdMemberIds = userRepository.findByHouseholdId(householdId)
                .stream().map(User::getId).collect(Collectors.toSet());
        boolean hasNonMember = members.stream().anyMatch(m -> !householdMemberIds.contains(m.getId()));
        if (hasNonMember || members.size() != request.getMemberIds().size()) {
            throw new CustomException(BillErrorCode.BILL_3005);
        }

        int totalAmount = bills.stream().mapToInt(UtilityBill::getAmount).sum();
        int memberCount = members.size();
        int amountPerMember = memberCount > 0 ? totalAmount / memberCount : 0;
        int remainder = memberCount > 0 ? totalAmount % memberCount : 0;

        Settlement settlement = settlementRepository.save(
                Settlement.builder()
                        .household(bills.get(0).getHousehold())
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

        bills.forEach(bill -> bill.requestSettlement(settlement));

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
                .items(bills.stream()
                        .map(b -> CreateBillSettlementResponse.BillItemResponse.builder()
                                .billId(b.getId())
                                .utilityTypeName(b.getBillType().getLabel())
                                .amount(b.getAmount())
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
}
