package com.partition.domain.supply.service;

import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.supply.dto.response.SupplyCategoryResponse;
import com.partition.domain.supply.repository.SupplyCategoryRepository;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Household;
import com.partition.entity.SupplyCategory;
import com.partition.entity.User;
import com.partition.entity.enums.SupplyCategoryType;
import com.partition.entity.enums.SupplySubCategoryType;
import com.partition.entity.enums.UserRole;
import com.partition.global.exception.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplyCategoryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private SupplyCategoryRepository supplyCategoryRepository;

    @InjectMocks
    private SupplyCategoryService supplyCategoryService;

    @Test
    void initializeDefaultCategories_savesEveryDefaultSubCategory() {
        Household household = Household.builder()
                .name("우리 집")
                .inviteCode("ABC123")
                .build();
        ReflectionTestUtils.setField(household, "id", 1L);

        supplyCategoryService.initializeDefaultCategories(household);

        ArgumentCaptor<SupplyCategory> captor = ArgumentCaptor.forClass(SupplyCategory.class);
        verify(supplyCategoryRepository, times(SupplySubCategoryType.values().length)).save(captor.capture());

        List<SupplyCategory> savedCategories = captor.getAllValues();
        assertThat(savedCategories).hasSize(SupplySubCategoryType.values().length);
        assertThat(savedCategories)
                .allMatch(category -> category.getHousehold().getId().equals(1L))
                .allMatch(category -> category.getCategory() == category.getSubCategory().getCategoryType());
    }

    @Test
    void getCategories_groupsByParentCategory() {
        User user = User.builder()
                .email("user@test.com")
                .name("tester")
                .provider("KAKAO")
                .providerId("123")
                .memberRole(UserRole.MEMBER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "householdId", 10L);

        Household household = Household.builder()
                .name("우리 집")
                .inviteCode("ABC123")
                .build();
        ReflectionTestUtils.setField(household, "id", 10L);

        SupplyCategory kitchen1 = SupplyCategory.builder()
                .household(household)
                .category(SupplyCategoryType.KITCHEN)
                .subCategory(SupplySubCategoryType.SCISSORS)
                .build();
        SupplyCategory kitchen2 = SupplyCategory.builder()
                .household(household)
                .category(SupplyCategoryType.KITCHEN)
                .subCategory(SupplySubCategoryType.KNIFE)
                .build();
        SupplyCategory bathroom = SupplyCategory.builder()
                .household(household)
                .category(SupplyCategoryType.BATHROOM)
                .subCategory(SupplySubCategoryType.SHAMPOO)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(householdRepository.findById(10L)).thenReturn(Optional.of(household));
        when(supplyCategoryRepository.findAllByHouseholdIdOrderByCategoryAscSubCategoryAsc(10L))
                .thenReturn(List.of(kitchen1, kitchen2, bathroom));

        List<SupplyCategoryResponse> result = supplyCategoryService.getCategories(1L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCategory()).isEqualTo("KITCHEN");
        assertThat(result.get(0).getCategoryName()).isEqualTo("주방용품");
        assertThat(result.get(0).getSubCategories()).hasSize(2);
        assertThat(result.get(1).getCategory()).isEqualTo("BATHROOM");
    }

    @Test
    void getCategories_throwsWhenNoCategoriesRegistered() {
        User user = User.builder()
                .email("user@test.com")
                .name("tester")
                .provider("KAKAO")
                .providerId("123")
                .memberRole(UserRole.MEMBER)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "householdId", 10L);

        Household household = Household.builder()
                .name("우리 집")
                .inviteCode("ABC123")
                .build();
        ReflectionTestUtils.setField(household, "id", 10L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(householdRepository.findById(10L)).thenReturn(Optional.of(household));
        when(supplyCategoryRepository.findAllByHouseholdIdOrderByCategoryAscSubCategoryAsc(10L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> supplyCategoryService.getCategories(1L))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(com.partition.domain.supply.exception.SupplyErrorCode.SUPPLY_1005);
    }
}
