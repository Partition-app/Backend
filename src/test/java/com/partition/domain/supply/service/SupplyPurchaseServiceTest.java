package com.partition.domain.supply.service;

import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.supply.dto.request.CreateSupplyPurchaseRequest;
import com.partition.domain.supply.dto.response.CreateSupplyPurchaseResponse;
import com.partition.domain.supply.dto.response.SupplyPurchaseListResponse;
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
import com.partition.entity.enums.UserRole;
import com.partition.global.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyPurchaseServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private SupplyCategoryRepository supplyCategoryRepository;

    @Mock
    private SupplyPurchaseRepository supplyPurchaseRepository;

    @InjectMocks
    private SupplyPurchaseService supplyPurchaseService;

    @Test
    void createPurchase_savesPurchaseSuccessfully() {
        CreateSupplyPurchaseRequest request = request(
                "콘푸라이트 500g",
                "2025-05-04",
                5980,
                3,
                "GROCERY",
                "RICE"
        );

        User user = user(1L, 10L);
        Household household = household(10L, "우리 집");
        SupplyCategory supplyCategory = SupplyCategory.builder()
                .household(household)
                .category(SupplyCategoryType.GROCERY)
                .subCategory(SupplySubCategoryType.RICE)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(householdRepository.findById(10L)).thenReturn(Optional.of(household));
        when(supplyCategoryRepository.findByHouseholdIdAndCategoryAndSubCategory(
                10L,
                SupplyCategoryType.GROCERY,
                SupplySubCategoryType.RICE
        )).thenReturn(Optional.of(supplyCategory));
        when(supplyPurchaseRepository.save(any(SupplyPurchase.class))).thenAnswer(invocation -> {
            SupplyPurchase purchase = invocation.getArgument(0);
            ReflectionTestUtils.setField(purchase, "id", 100L);
            ReflectionTestUtils.setField(purchase, "createdAt", LocalDateTime.of(2025, 5, 4, 14, 30));
            return purchase;
        });

        CreateSupplyPurchaseResponse response = supplyPurchaseService.createPurchase(1L, request);

        assertThat(response.getPurchaseId()).isEqualTo(100L);
        assertThat(response.getItemName()).isEqualTo("콘푸라이트 500g");
        assertThat(response.getPurchaseDate()).isEqualTo(LocalDate.of(2025, 5, 4));
        assertThat(response.getAmount()).isEqualTo(5980);
        assertThat(response.getQuantity()).isEqualTo(3);
        assertThat(response.getCategory()).isEqualTo("GROCERY");
        assertThat(response.getSubCategory()).isEqualTo("RICE");
    }

    @Test
    void createPurchase_throwsWhenCategoryInvalid() {
        CreateSupplyPurchaseRequest request = request(
                "콘푸라이트 500g",
                "2025-05-04",
                5980,
                3,
                "INVALID",
                "RICE"
        );

        assertThatThrownBy(() -> supplyPurchaseService.createPurchase(1L, request))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(SupplyErrorCode.SUPPLY_3006);
    }

    @Test
    void getPurchases_returnsPurchasesInDateRange() {
        User user = user(1L, 10L);
        Household household = household(10L, "우리 집");

        SupplyPurchase purchase1 = purchase(1L, household, "콘푸라이트 500g", LocalDate.of(2025, 5, 4), 5980, 3, true);
        SupplyPurchase purchase2 = purchase(2L, household, "두루마리 휴지", LocalDate.of(2025, 5, 5), 8000, 3, false);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(householdRepository.findById(10L)).thenReturn(Optional.of(household));
        when(supplyPurchaseRepository.findAllByHouseholdIdAndPurchaseDateBetweenOrderByPurchaseDateAscIdAsc(
                10L,
                LocalDate.of(2025, 5, 4),
                LocalDate.of(2025, 5, 5)
        )).thenReturn(List.of(purchase1, purchase2));

        SupplyPurchaseListResponse response = supplyPurchaseService.getPurchases(1L, "2025-05-04", "2025-05-05");

        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(response.getPurchases()).hasSize(2);
        assertThat(response.getPurchases().get(0).getIsSettled()).isTrue();
        assertThat(response.getPurchases().get(1).getIsSettled()).isFalse();
    }

    @Test
    void getPurchases_throwsWhenEndDateBeforeStartDate() {
        assertThatThrownBy(() -> supplyPurchaseService.getPurchases(1L, "2025-05-05", "2025-05-04"))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(SupplyErrorCode.SUPPLY_2003);
    }

    private CreateSupplyPurchaseRequest request(
            String itemName,
            String purchaseDate,
            Integer amount,
            Integer quantity,
            String category,
            String subCategory
    ) {
        CreateSupplyPurchaseRequest request = new CreateSupplyPurchaseRequest();
        ReflectionTestUtils.setField(request, "itemName", itemName);
        ReflectionTestUtils.setField(request, "purchaseDate", purchaseDate);
        ReflectionTestUtils.setField(request, "amount", amount);
        ReflectionTestUtils.setField(request, "quantity", quantity);
        ReflectionTestUtils.setField(request, "category", category);
        ReflectionTestUtils.setField(request, "subCategory", subCategory);
        return request;
    }

    private User user(Long id, Long householdId) {
        User user = User.builder()
                .email("user@test.com")
                .name("tester")
                .provider("KAKAO")
                .providerId("123")
                .memberRole(UserRole.MEMBER)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "householdId", householdId);
        return user;
    }

    private Household household(Long id, String name) {
        Household household = Household.builder()
                .name(name)
                .inviteCode("ABC123")
                .build();
        ReflectionTestUtils.setField(household, "id", id);
        return household;
    }

    private SupplyPurchase purchase(
            Long id,
            Household household,
            String itemName,
            LocalDate purchaseDate,
            Integer amount,
            Integer quantity,
            Boolean isSettled
    ) {
        SupplyCategory supplyCategory = SupplyCategory.builder()
                .household(household)
                .category(SupplyCategoryType.GROCERY)
                .subCategory(SupplySubCategoryType.RICE)
                .build();

        SupplyPurchase purchase = SupplyPurchase.builder()
                .household(household)
                .supplyCategory(supplyCategory)
                .itemName(itemName)
                .purchaseDate(purchaseDate)
                .amount(amount)
                .quantity(quantity)
                .isSettled(isSettled)
                .build();
        ReflectionTestUtils.setField(purchase, "id", id);
        return purchase;
    }
}
