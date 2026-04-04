package com.partition.domain.supply.service;

import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.supply.dto.request.CreateSupplyPurchaseRequest;
import com.partition.domain.supply.dto.response.CreateSupplyPurchaseResponse;
import com.partition.domain.supply.dto.response.SupplyPurchaseListResponse;
import com.partition.domain.supply.dto.response.SupplyPurchaseResultResponse;
import com.partition.domain.supply.exception.SupplyErrorCode;
import com.partition.domain.supply.repository.SupplyCategoryRepository;
import com.partition.domain.supply.repository.SupplyPurchaseRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Household;
import com.partition.entity.SupplyCategory;
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
    private final SupplyCategoryRepository supplyCategoryRepository;
    private final SupplyPurchaseRepository supplyPurchaseRepository;

    @Transactional
    public CreateSupplyPurchaseResponse createPurchase(Long userId, CreateSupplyPurchaseRequest request) {
        validateCreateRequest(request);

        User user = getUser(userId);
        Household household = getHousehold(user);

        SupplyCategoryType categoryType = parseCategory(request.getCategory());
        SupplySubCategoryType subCategoryType = parseSubCategory(request.getSubCategory());

        SupplyCategory supplyCategory = supplyCategoryRepository
                .findByHouseholdIdAndCategoryAndSubCategory(household.getId(), categoryType, subCategoryType)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3006));

        SupplyPurchase purchase = supplyPurchaseRepository.save(
                SupplyPurchase.builder()
                        .household(household)
                        .supplyCategory(supplyCategory)
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
                .category(supplyCategory.getCategory().name())
                .subCategory(supplyCategory.getSubCategory().name())
                .createdAt(purchase.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public SupplyPurchaseListResponse getPurchases(Long userId, String startDate, String endDate) {
        LocalDate parsedStartDate = parseStartDate(startDate);
        LocalDate parsedEndDate = parseEndDate(endDate);

        if (parsedEndDate.isBefore(parsedStartDate)) {
            throw new CustomException(SupplyErrorCode.SUPPLY_2003);
        }

        User user = getUser(userId);
        Household household = getHousehold(user);

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
                        .build())
                .toList();

        return SupplyPurchaseListResponse.builder()
                .totalCount(purchases.size())
                .purchases(purchases)
                .build();
    }

    private void validateCreateRequest(CreateSupplyPurchaseRequest request) {
        if (request.getItemName() == null || request.getItemName().trim().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3001);
        }

        if (request.getPurchaseDate() == null || request.getPurchaseDate().trim().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3002);
        }

        parsePurchaseDate(request.getPurchaseDate());

        if (request.getAmount() == null || request.getAmount() < 0) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3004);
        }

        if (request.getQuantity() == null || request.getQuantity() < 1) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3005);
        }

        parseCategory(request.getCategory());
        parseSubCategory(request.getSubCategory());
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3007));
    }

    private Household getHousehold(User user) {
        if (user.getHouseholdId() == null) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3008);
        }

        return householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(SupplyErrorCode.SUPPLY_3008));
    }

    private LocalDate parsePurchaseDate(String purchaseDate) {
        try {
            return LocalDate.parse(purchaseDate);
        } catch (DateTimeParseException e) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3003);
        }
    }

    private LocalDate parseStartDate(String startDate) {
        if (startDate == null || startDate.trim().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_2001);
        }

        return parseQueryDate(startDate);
    }

    private LocalDate parseEndDate(String endDate) {
        if (endDate == null || endDate.trim().isEmpty()) {
            throw new CustomException(SupplyErrorCode.SUPPLY_2002);
        }

        return parseQueryDate(endDate);
    }

    private LocalDate parseQueryDate(String date) {
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new CustomException(SupplyErrorCode.SUPPLY_2004);
        }
    }

    private SupplyCategoryType parseCategory(String category) {
        try {
            return SupplyCategoryType.valueOf(category);
        } catch (Exception e) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3006);
        }
    }

    private SupplySubCategoryType parseSubCategory(String subCategory) {
        try {
            return SupplySubCategoryType.valueOf(subCategory);
        } catch (Exception e) {
            throw new CustomException(SupplyErrorCode.SUPPLY_3006);
        }
    }
}
