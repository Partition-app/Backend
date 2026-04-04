package com.partition.domain.household.service;

import com.partition.domain.chore.repository.HouseholdChoreRepository;
import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.supply.service.SupplyCategoryService;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Household;
import com.partition.entity.User;
import com.partition.entity.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HouseholdServiceTest {

    @Mock
    private HouseholdRepository householdRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HouseholdChoreRepository householdChoreRepository;

    @Mock
    private SupplyCategoryService supplyCategoryService;

    @InjectMocks
    private HouseholdService householdService;

    @Test
    void createHousehold_initializesDefaultSupplyCategories() {
        User user = User.builder()
                .email("user@test.com")
                .name("tester")
                .provider("KAKAO")
                .providerId("123")
                .memberRole(UserRole.GUEST)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(householdRepository.saveAndFlush(any(Household.class))).thenAnswer(invocation -> {
            Household household = invocation.getArgument(0);
            ReflectionTestUtils.setField(household, "id", 10L);
            return household;
        });

        Household household = householdService.createHousehold(1L, "우리 집");

        ArgumentCaptor<Household> householdCaptor = ArgumentCaptor.forClass(Household.class);
        verify(supplyCategoryService).initializeDefaultCategories(householdCaptor.capture());

        assertThat(household.getId()).isEqualTo(10L);
        assertThat(household.getName()).isEqualTo("우리 집");
        assertThat(household.getInviteCode()).hasSize(6);
        assertThat(householdCaptor.getValue().getId()).isEqualTo(10L);
        assertThat(user.getHouseholdId()).isEqualTo(10L);
        assertThat(user.getMemberRole()).isEqualTo(UserRole.LEADER);
    }
}
