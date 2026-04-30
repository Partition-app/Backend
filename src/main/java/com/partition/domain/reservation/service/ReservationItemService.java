package com.partition.domain.reservation.service;

import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.reservation.dto.request.CreateReservationItemRequest;
import com.partition.domain.reservation.dto.response.CreateReservationItemResponse;
import com.partition.domain.reservation.exception.ReservationErrorCode;
import com.partition.domain.reservation.repository.ReservationItemRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Household;
import com.partition.entity.ReservationItem;
import com.partition.entity.User;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationItemService {

    private final ReservationItemRepository reservationItemRepository;
    private final UserRepository userRepository;
    private final HouseholdRepository householdRepository;

    @Transactional
    public CreateReservationItemResponse createItem(Long userId, CreateReservationItemRequest request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new CustomException(ReservationErrorCode.RESERVATION_1001);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            throw new CustomException(UserErrorCode.HAVE_NO_GROUP);
        }

        Household household = householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(UserErrorCode.HAVE_NO_GROUP));

        if (reservationItemRepository.existsByHouseholdAndName(household, request.getName())) {
            throw new CustomException(ReservationErrorCode.RESERVATION_1002);
        }

        ReservationItem item = ReservationItem.builder()
                .household(household)
                .name(request.getName())
                .build();

        reservationItemRepository.save(item);

        return CreateReservationItemResponse.from(item);
    }
}
