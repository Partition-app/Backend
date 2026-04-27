package com.partition.domain.supply.service;

import com.partition.domain.alarm.service.AlarmService;
import com.partition.domain.supply.dto.request.CreateSettlementRequest;
import com.partition.domain.supply.dto.response.ConfirmSettlementResponse;
import com.partition.domain.supply.dto.response.CreateSettlementResponse;
import com.partition.domain.supply.dto.response.SettlementRequestedListResponse;
import com.partition.domain.supply.dto.response.SupplySettlementDetailResponse;
import com.partition.domain.supply.exception.SupplyErrorCode;
import com.partition.domain.supply.repository.SettlementMemberRepository;
import com.partition.domain.supply.repository.SettlementRepository;
import com.partition.domain.supply.repository.SupplyPurchaseRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.*;
import com.partition.entity.enums.AlarmType;
import com.partition.entity.enums.SupplyPurchaseStatus;

import java.util.stream.Collectors;
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
    private final AlarmService alarmService;

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

        // 이미 정산요청된 구매 기록 포함 여부 검증
        boolean hasSettlementRequested = purchases.stream()
                .anyMatch(p -> p.getStatus() == SupplyPurchaseStatus.REQUESTED);
        if (hasSettlementRequested) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5003);
        }

        // 이미 정산완료된 구매 기록 포함 여부 검증
        boolean hasAlreadySettled = purchases.stream()
                .anyMatch(p -> p.getStatus() == SupplyPurchaseStatus.SETTLED);
        if (hasAlreadySettled) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5006);
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

        // 구매 기록 정산 요청 처리
        purchases.forEach(purchase -> purchase.requestSettlement(settlement));

        alarmService.createSettlementAlarms(savedMembers, settlement.getId(), AlarmType.SUPPLY_SETTLEMENT_REQUESTED);

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

    /**
     * 정산 완료 처리
     */
    @Transactional
    public ConfirmSettlementResponse confirmSettlement(Long userId, Long settlementId) {
        // 정산 내역 조회
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_5007));

        // 권한 검증 (요청 유저가 해당 하우스 멤버인지)
        User requestUser = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3007));
        if (!settlement.getHousehold().getId().equals(requestUser.getHouseholdId())) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5010);
        }

        // 이미 정산완료된 내역 검증
        if (Boolean.TRUE.equals(settlement.getIsConfirmed())) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5008);
        }

        // 정산요청 상태 검증 (연결된 구매 기록이 모두 정산요청 상태여야 함)
        List<SupplyPurchase> purchases = supplyPurchaseRepository.findAllBySettlementId(settlementId);
        boolean hasNonRequested = purchases.stream().anyMatch(p -> p.getStatus() != SupplyPurchaseStatus.REQUESTED);
        if (hasNonRequested) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5009);
        }

        // 정산 완료 처리
        settlement.confirm();
        purchases.forEach(purchase -> purchase.settle(settlement));

        // 멤버 정보 조회
        List<SettlementMember> members = settlementMemberRepository.findAllBySettlementId(settlementId);

        alarmService.createSettlementAlarms(members, settlement.getId(), AlarmType.SUPPLY_SETTLEMENT_CONFIRMED);

        return ConfirmSettlementResponse.builder()
                .settlementId(settlement.getId())
                .totalAmount(settlement.getTotalAmount())
                .memberCount(settlement.getMemberCount())
                .amountPerMember(settlement.getAmountPerMember())
                .confirmedAt(settlement.getConfirmedAt())
                .members(members.stream()
                        .map(sm -> ConfirmSettlementResponse.SettlementMemberResponse.builder()
                                .userId(sm.getUser().getId())
                                .name(sm.getUser().getName())
                                .amount(sm.getAmount())
                                .build())
                        .toList())
                .items(purchases.stream()
                        .map(p -> ConfirmSettlementResponse.SettlementItemResponse.builder()
                                .purchaseId(p.getId())
                                .itemName(p.getItemName())
                                .amount(p.getAmount())
                                .build())
                        .toList())
                .build();
    }

    @Transactional(readOnly = true)
    public SettlementRequestedListResponse getRequestedSettlements(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3007));
        if (user.getHouseholdId() == null) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3008);
        }

        List<SettlementRequestedListResponse.SettlementItem> settlements =
                supplyPurchaseRepository.findAllByHouseholdIdAndStatus(user.getHouseholdId(), SupplyPurchaseStatus.REQUESTED)
                        .stream()
                        .collect(Collectors.groupingBy(p -> p.getSettlement().getId()))
                        .values().stream()
                        .map(purchases -> {
                            Settlement s = purchases.get(0).getSettlement();
                            return SettlementRequestedListResponse.SettlementItem.builder()
                                    .settlementId(s.getId())
                                    .totalAmount(s.getTotalAmount())
                                    .amountPerMember(s.getAmountPerMember())
                                    .memberCount(s.getMemberCount())
                                    .requestedAt(s.getCreatedAt())
                                    .build();
                        })
                        .toList();

        return SettlementRequestedListResponse.builder()
                .totalCount(settlements.size())
                .settlements(settlements)
                .build();
    }

    @Transactional(readOnly = true)
    public SupplySettlementDetailResponse getSettlementDetail(Long userId, Long settlementId) {
        Settlement settlement = settlementRepository.findById(settlementId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_5007));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3007));
        if (!settlement.getHousehold().getId().equals(user.getHouseholdId())) {
            throw new CustomException(SupplyErrorCode.SUPPLY_5010);
        }

        List<SettlementMember> members = settlementMemberRepository.findAllBySettlementId(settlementId);
        List<SupplyPurchase> purchases = supplyPurchaseRepository.findAllBySettlementId(settlementId);

        return SupplySettlementDetailResponse.builder()
                .settlementId(settlement.getId())
                .totalAmount(settlement.getTotalAmount())
                .amountPerMember(settlement.getAmountPerMember())
                .memberCount(settlement.getMemberCount())
                .isConfirmed(settlement.getIsConfirmed())
                .confirmedAt(settlement.getConfirmedAt())
                .members(members.stream()
                        .map(sm -> SupplySettlementDetailResponse.MemberItem.builder()
                                .userId(sm.getUser().getId())
                                .name(sm.getUser().getName())
                                .amount(sm.getAmount())
                                .build())
                        .toList())
                .items(purchases.stream()
                        .map(p -> SupplySettlementDetailResponse.PurchaseItem.builder()
                                .purchaseId(p.getId())
                                .itemName(p.getItemName())
                                .purchaseDate(p.getPurchaseDate())
                                .amount(p.getAmount())
                                .build())
                        .toList())
                .build();
    }
}
