package com.partition.domain.supply.service;

import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.supply.dto.request.CreateSupplyPurchaseRequest;
import com.partition.domain.supply.dto.request.UpdateSupplyPurchaseRequest;
import com.partition.domain.supply.dto.response.CreateSupplyPurchaseResponse;
import com.partition.domain.supply.dto.response.SettlementListResponse;
import com.partition.domain.supply.dto.response.SettlementPurchaseResponse;
import com.partition.domain.supply.dto.response.SupplyPurchaseListResponse;
import com.partition.domain.supply.dto.response.SupplyPurchaseResultResponse;
import com.partition.domain.supply.dto.response.UpdateSupplyPurchaseResponse;
import com.partition.domain.supply.exception.SupplyErrorCode;
import com.partition.domain.supply.repository.SupplyPurchaseRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Household;
import com.partition.entity.SupplyPurchase;
import com.partition.entity.User;
import com.partition.entity.enums.SupplyCategoryType;
import com.partition.entity.enums.SupplySubCategoryType;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplyPurchaseService {

    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;
    private final SupplyPurchaseRepository supplyPurchaseRepository;

    /**
     * 구매 내역 등록
     * 요청값 검증 -> 유저/가구 확인 -> 카테고리 조회 -? 저장
     */
    @Transactional
    public CreateSupplyPurchaseResponse createPurchase(Long userId, CreateSupplyPurchaseRequest request) {
        // 요청값 유효성 검증
        validateCreateRequest(request);

        // 유저 및 가구 조회
        User user = getUser(userId);
        Household household = getHousehold(user);

        // 카테고리/서브카테고리 enum 변환
        SupplyCategoryType categoryType = parseCategory(request.getCategory());
        SupplySubCategoryType subCategoryType = parseSubCategory(request.getSubCategory());

        // 구매 내역 저장
        SupplyPurchase purchase = supplyPurchaseRepository.save(
                SupplyPurchase.builder()
                        .household(household)
                        .category(categoryType)
                        .subCategory(subCategoryType)
                        .itemName(request.getItemName().trim())
                        .purchaseDate(parsePurchaseDate(request.getPurchaseDate()))
                        .amount(request.getAmount())
                        .quantity(request.getQuantity())
                        .isSettled(false)
                        .build()
        );

        return CreateSupplyPurchaseResponse.builder()
                .purchaseId(purchase.getId())
                .itemName(purchase.getItemName())
                .purchaseDate(purchase.getPurchaseDate())
                .amount(purchase.getAmount())
                .quantity(purchase.getQuantity())
                .category(purchase.getCategory().name())
                .subCategory(purchase.getSubCategory().name())
                .createdAt(purchase.getCreatedAt())
                .build();
    }

    /**
     * 기간별 구매 내역 조회
     */
    @Transactional(readOnly = true)
    public SupplyPurchaseListResponse getPurchases(Long userId, String startDate, String endDate) {
        // 조회 기간 파싱
        LocalDate parsedStartDate = parseStartDate(startDate);
        LocalDate parsedEndDate = parseEndDate(endDate);

        // 날짜 역전 검증
        if (parsedEndDate.isBefore(parsedStartDate)) {
            throw new CustomException(SupplyErrorCode.SUPPLY_2003);
        }

        // 유저 및 가구 조회
        User user = getUser(userId);
        Household household = getHousehold(user);

        // 기간 내 구매 내역 조회 (날짜 -> id 오름차순)
        List<SupplyPurchaseResultResponse> purchases = supplyPurchaseRepository
                .findAllByHouseholdIdAndPurchaseDateBetweenOrderByPurchaseDateAscIdAsc(
                        household.getId(),
                        parsedStartDate,
                        parsedEndDate
                )
                .stream()
                .map(purchase -> SupplyPurchaseResultResponse.builder()
                        .purchaseId(purchase.getId())
                        .itemName(purchase.getItemName())
                        .purchaseDate(purchase.getPurchaseDate())
                        .amount(purchase.getAmount())
                        .quantity(purchase.getQuantity())
                        .isSettled(purchase.getIsSettled())
                        .category(purchase.getSupplyCategory().getCategory().name())
                        .subCategory(purchase.getSupplyCategory().getSubCategory().name())
                        .build())
                .toList();

        // 총 건수 + 목록 묶어서 반환
        return SupplyPurchaseListResponse.builder()
                .totalCount(purchases.size())
                .purchases(purchases)
                .build();
    }

    /**
     * 정산 대상 구매 내역 조회 (isSettled = false 인 물품)
     */
    @Transactional(readOnly = true)
    public SettlementListResponse getSettlementPurchases(Long userId, String startDate, String endDate) {
        // 조회 기간 파싱
        LocalDate parsedStartDate = parseStartDate(startDate);
        LocalDate parsedEndDate = parseEndDate(endDate);

        // 날짜 검증
        if (parsedEndDate.isBefore(parsedStartDate)) {
            throw new CustomException(SupplyErrorCode.SUPPLY_2003);
        }

        // 유저 및 가구 조회
        User user = getUser(userId);
        Household household = getHousehold(user);

        // 가구 멤버 수 조회
        int memberCount = userRepository.findByHouseholdId(household.getId()).size();

        // 미정산 구매 내역 조회
        List<SettlementPurchaseResponse> purchases = supplyPurchaseRepository
                .findAllByHouseholdIdAndIsSettledFalseAndPurchaseDateBetweenOrderByPurchaseDateAscIdAsc(
                        household.getId(),
                        parsedStartDate,
                        parsedEndDate
                )
                .stream()
                .map(purchase -> SettlementPurchaseResponse.builder()
                        .purchaseId(purchase.getId())
                        .itemName(purchase.getItemName())
                        .purchaseDate(purchase.getPurchaseDate())
                        .amount(purchase.getAmount())
                        .quantity(purchase.getQuantity())
                        .build())
                .toList();


        // 정산
        int totalAmount = purchases.stream().mapToInt(SettlementPurchaseResponse::getAmount).sum();
        int amountPerMember = memberCount > 0 ? totalAmount / memberCount : 0;
        int remainder = memberCount > 0 ? totalAmount % memberCount : 0;

        return SettlementListResponse.builder()
                .totalCount(purchases.size())
                .totalAmount(totalAmount)
                .amountPerMember(amountPerMember)
                .remainder(remainder)
                .memberCount(memberCount)
                .purchases(purchases)
                .build();
    }

    /**
     * 구매 내역 수정 (변경된 필드만 반영)
     */
    @Transactional
    public UpdateSupplyPurchaseResponse updatePurchase(Long userId, Long purchaseId, UpdateSupplyPurchaseRequest request) {
        // 구매 내역 조회
        SupplyPurchase purchase = supplyPurchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3009));

        // 호출자 가구 소유권 확인
        User caller = getUser(userId);
        Household callerHousehold = getHousehold(caller);
        if (!callerHousehold.getId().equals(purchase.getHousehold().getId())) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3010);
        }

        // 각 필드 유효성 검증 (전달된 경우에만)
        String trimmedItemName = null;
        if (request.getItemName() != null) {
            if (request.getItemName().trim().isEmpty()) {
                throw new CustomException(SupplyErrorCode.SUPPLY_3001);
            }
            trimmedItemName = request.getItemName().trim();
        }

        LocalDate purchaseDate = null;
        if (request.getPurchaseDate() != null) {
            purchaseDate = parsePurchaseDate(request.getPurchaseDate());
        }

        if (request.getAmount() != null && request.getAmount() < 0) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3004);
        }

        if (request.getQuantity() != null && request.getQuantity() < 1) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3005);
        }

        // 카테고리 변경 시 enum 변환
        SupplyCategoryType categoryType = null;
        SupplySubCategoryType subCategoryType = null;
        if (request.getCategory() != null || request.getSubCategory() != null) {
            categoryType = parseCategory(
                    request.getCategory() != null ? request.getCategory() : purchase.getCategory().name()
            );
            subCategoryType = parseSubCategory(
                    request.getSubCategory() != null ? request.getSubCategory() : purchase.getSubCategory().name()
            );
        }

        // 구매 내역 업데이트
        purchase.update(trimmedItemName, purchaseDate, request.getAmount(), request.getQuantity(), categoryType, subCategoryType);

        return UpdateSupplyPurchaseResponse.builder()
                .purchaseId(purchase.getId())
                .itemName(purchase.getItemName())
                .purchaseDate(purchase.getPurchaseDate())
                .amount(purchase.getAmount())
                .quantity(purchase.getQuantity())
                .category(purchase.getCategory().name())
                .subCategory(purchase.getSubCategory().name())
                .updatedAt(purchase.getUpdatedAt())
                .build();
    }

    private void validateCreateRequest(CreateSupplyPurchaseRequest request) {
        // 상품명 공백 포함 빈값 체크
        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3001);
        }

        // 구매일 빈값 체크
        if (request.getPurchaseDate() == null || request.getPurchaseDate().trim().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3002);
        }

        // 구매일 포맷 검증 (파싱 결과는 버리고 예외만 확인)
        parsePurchaseDate(request.getPurchaseDate());

        // 금액 음수 체크
        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3004);
        }

        // 수량 최소값 체크
        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3005);
        }

        // 카테고리/서브카테고리 enum 변환 가능 여부 검증
        parseCategory(request.getCategory());
        parseSubCategory(request.getSubCategory());
    }

    // 유저 조회
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3007));
    }

    // 가구 소속 여부 및 가구 실존 여부 검증
    private Household getHousehold(User user) {
        if (user.getHouseholdId() == null) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3008);
        }

        return householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3008));
    }

    // 구매일 파싱 (ISO_LOCAL_DATE 포맷)
    private LocalDate parsePurchaseDate(String purchaseDate) {
        try {
            return LocalDate.parse(purchaseDate);
        } catch (DateTimeParseException e) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3003);
        }
    }

    // 조회 시작일 파싱
    private LocalDate parseStartDate(String startDate) {
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_2001);
        }

        return parseQueryDate(startDate);
    }

    // 조회 종료일 파싱
    private LocalDate parseEndDate(String endDate) {
        if (endDate == null || endDate.trim().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_2002);
        }

        return parseQueryDate(endDate);
    }

    // 조회 날짜 파싱 공통 처리
    private LocalDate parseQueryDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new CustomException(SupplyErrorCode.SUPPLY_2004);
        }
    }

    // 카테고리 enum 변환
    private SupplyCategoryType parseCategory(String category) {
        try {
            return SupplyCategoryType.valueOf(category);
        } catch (Exception e) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3006);
        }
    }

    // 서브카테고리 enum 변환
    private SupplySubCategoryType parseSubCategory(String subCategory) {
        try {
            return SupplySubCategoryType.valueOf(subCategory);
        } catch (Exception e) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3006);
        }
    }
}
