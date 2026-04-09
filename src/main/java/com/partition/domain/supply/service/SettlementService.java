package com.partition.domain.supply.service;

import com.partition.domain.supply.dto.request.CreateSettlementRequest;
import com.partition.domain.supply.dto.response.CreateSettlementResponse;
import com.partition.domain.supply.exception.SupplyErrorCode;
import com.partition.domain.supply.repository.SettlementMemberRepository;
import com.partition.domain.supply.repository.SettlementRepository;
import com.partition.domain.supply.repository.SupplyPurchaseRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.*;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {

    private final UserRepository userRepository;
    private final SupplyPurchaseRepository supplyPurchaseRepository;
    private final SettlementRepository settlementRepository;
    private final SettlementMemberRepository settlementMemberRepository;

    /**
     * 정산 내역 만들고 저장
     */
    @Transactional
    public CreateSettlementResponse createSettlement(Long userId, CreateSettlementRequest request) {
        // 구매 기록 ID 누락 검증
        if (request.getPurchaseIds() == null || request.getPurchaseIds().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5001);
        }

        // 멤버 선택 누락 검증
        if (request.getMemberIds() == null || request.getMemberIds().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5005);
        }

        // 요청 유저 및 가구 조회
        User requestUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3007));
        if (requestUser.getHouseholdId() == null) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3008);
        }
        Long householdId = requestUser.getHouseholdId();

        // 구매 기록 조회 (존재 여부 검증)
        List<SupplyPurchase> purchases = supplyPurchaseRepository.findAllById(request.getPurchaseIds());
        if (purchases.size() != request.getPurchaseIds().size()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5002);
        }

        // 이미 정산된 구매 기록 포함 여부 검증
        boolean hasAlreadySettled = purchases.stream().anyMatch(SupplyPurchase::getIsSettled);
        if (hasAlreadySettled) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5003);
        }

        // 멤버 조회 및 하우스 멤버 여부 검증
        List<User> members = userRepository.findAllById(request.getMemberIds());
        Set<Long> householdMemberIds = userRepository.findByHouseholdId(householdId)
                .stream().map(User::getId).collect(Collectors.toSet());
        boolean hasNonMember = members.stream().anyMatch(m -> !householdMemberIds.contains(m.getId()));
        if (hasNonMember || members.size() != request.getMemberIds().size()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5004);
        }

        // 정산 금액 계산
        int totalAmount = purchases.stream().mapToInt(SupplyPurchase::getAmount).sum();
        int memberCount = members.size();
        int amountPerMember = memberCount > 0 ? totalAmount / memberCount : 0;
        int remainder = memberCount > 0 ? totalAmount % memberCount : 0;

        // Settlement 저장
        Household household = purchases.get(0).getHousehold();
        Settlement settlement = settlementRepository.save(
                Settlement.builder()
                        .household(household)
                        .totalAmount(totalAmount)
                        .memberCount(memberCount)
                        .amountPerMember(amountPerMember)
                        .build()
        );

        // SettlementMember 저장 (첫 remainder명에게 +1원 배정)
        List<SettlementMember> savedMembers = new java.util.ArrayList<>();
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

        // 구매 기록 정산 처리
        purchases.forEach(purchase -> purchase.settle(settlement));

        return CreateSettlementResponse.builder()
                .settlementId(settlement.getId())
                .totalAmount(totalAmount)
                .memberCount(memberCount)
                .amountPerMember(amountPerMember)
                .settledAt(settlement.getCreatedAt())
                .members(savedMembers.stream()
                        .map(sm -> CreateSettlementResponse.SettlementMemberResponse.builder()
                                .userId(sm.getUser().getId())
                                .name(sm.getUser().getName())
                                .amount(sm.getAmount())
                                .build())
                        .toList())
                .items(purchases.stream()
                        .map(p -> CreateSettlementResponse.SettlementItemResponse.builder()
                                .purchaseId(p.getId())
                                .itemName(p.getItemName())
                                .amount(p.getAmount())
                                .build())
                        .toList())
                .build();
    }
}
