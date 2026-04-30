package com.partition.domain.reservation.service;

import com.partition.domain.household.repository.HouseholdRepository;
import com.partition.domain.reservation.dto.request.CreateReservationItemRequest;
import com.partition.domain.reservation.dto.request.DeleteReservationItemRequest;
import com.partition.domain.reservation.dto.request.UpdateReservationItemRequest;
import com.partition.domain.reservation.dto.response.CreateReservationItemResponse;
import com.partition.domain.reservation.dto.response.ReservationItemResponse;
import com.partition.domain.reservation.dto.response.UpdateReservationItemResponse;
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

import java.util.List;

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

    @Transactional(readOnly = true)
    public List<ReservationItemResponse> getItems(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            throw new CustomException(UserErrorCode.HAVE_NO_GROUP);
        }

        Household household = householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(UserErrorCode.HAVE_NO_GROUP));

        return reservationItemRepository.findByHousehold(household).stream()
                .map(ReservationItemResponse::from)
                .toList();
    }

    @Transactional
    public UpdateReservationItemResponse updateItem(Long userId, Long itemId, UpdateReservationItemRequest request) {
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

        ReservationItem item = reservationItemRepository.findById(itemId)
                .filter(i -> i.getHousehold().getId().equals(household.getId()))
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_1003));

        if (reservationItemRepository.existsByHouseholdAndNameAndIdNot(household, request.getName(), itemId)) {
            throw new CustomException(ReservationErrorCode.RESERVATION_1002);
        }

        item.updateName(request.getName());

        return UpdateReservationItemResponse.from(item);
    }

    @Transactional
    public void deleteItems(Long userId, DeleteReservationItemRequest request) {
        if (request.getItemIds() == null || request.getItemIds().isEmpty()) {
            throw new CustomException(ReservationErrorCode.RESERVATION_1004);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            throw new CustomException(UserErrorCode.HAVE_NO_GROUP);
        }

        Household household = householdRepository.findById(user.getHouseholdId())
                .orElseThrow(() -> new CustomException(UserErrorCode.HAVE_NO_GROUP));

        List<ReservationItem> items = reservationItemRepository.findAllByIdInAndHousehold(request.getItemIds(), household);

        if (items.size() != request.getItemIds().size()) {
            throw new CustomException(ReservationErrorCode.RESERVATION_1003);
        }

        reservationItemRepository.deleteAll(items);
    }
}
