package com.partition.domain.reservation.service;

import com.partition.domain.reservation.dto.request.CreateReservationRequest;
import com.partition.domain.reservation.dto.request.DeleteReservationRequest;
import com.partition.domain.reservation.dto.request.UpdateReservationRequest;
import com.partition.domain.reservation.dto.response.CreateReservationResponse;
import com.partition.domain.reservation.dto.response.ReservationListItemResponse;
import com.partition.domain.reservation.exception.ReservationErrorCode;
import com.partition.domain.reservation.repository.ReservationItemRepository;
import com.partition.domain.reservation.repository.ReservationRepository;
import com.partition.domain.user.exception.UserErrorCode;
import com.partition.domain.user.repository.UserRepository;
import com.partition.entity.Reservation;
import com.partition.entity.ReservationItem;
import com.partition.entity.User;
import com.partition.global.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationItemRepository reservationItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public CreateReservationResponse createReservation(Long userId, CreateReservationRequest request) {
        if (request.getItemId() == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2001);
        }
        if (request.getStartTime() == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2002);
        }
        if (request.getEndTime() == null) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2003);
        }
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2004);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            throw new CustomException(UserErrorCode.HAVE_NO_GROUP);
        }

        ReservationItem item = reservationItemRepository.findById(request.getItemId())
                .filter(i -> i.getHousehold().getId().equals(user.getHouseholdId()))
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_1003));

        if (reservationRepository.existsOverlap(item, request.getStartTime(), request.getEndTime())) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2005);
        }

        Reservation reservation = Reservation.builder()
                .item(item)
                .user(user)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        reservationRepository.save(reservation);

        return CreateReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<ReservationListItemResponse> getReservations(Long userId, String startDate, String endDate) {
        LocalDate start = parseDate(startDate, ReservationErrorCode.RESERVATION_2006);
        LocalDate end = parseDate(endDate, ReservationErrorCode.RESERVATION_2007);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

        if (user.getHouseholdId() == null) {
            throw new CustomException(UserErrorCode.HAVE_NO_GROUP);
        }

        return reservationRepository.findByHouseholdIdAndDateRange(
                        user.getHouseholdId(),
                        start.atStartOfDay(),
                        end.plusDays(1).atStartOfDay()
                ).stream()
                .map(ReservationListItemResponse::from)
                .toList();
    }

    @Transactional
    public ReservationListItemResponse updateReservation(Long userId, Long reservationId, UpdateReservationRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_2008));

        if (!reservation.getUser().getId().equals(userId)) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2009);
        }

        Long householdId = reservation.getItem().getHousehold().getId();
        ReservationItem effectiveItem = request.getItemId() != null
                ? reservationItemRepository.findById(request.getItemId())
                        .filter(i -> i.getHousehold().getId().equals(householdId))
                        .orElseThrow(() -> new CustomException(ReservationErrorCode.RESERVATION_1003))
                : reservation.getItem();

        LocalDateTime effectiveStart = request.getStartTime() != null ? request.getStartTime() : reservation.getStartTime();
        LocalDateTime effectiveEnd = request.getEndTime() != null ? request.getEndTime() : reservation.getEndTime();

        if (!effectiveEnd.isAfter(effectiveStart)) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2004);
        }

        if (reservationRepository.existsOverlapExcluding(effectiveItem, reservationId, effectiveStart, effectiveEnd)) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2005);
        }

        if (request.getItemId() != null) reservation.updateItem(effectiveItem);
        if (request.getStartTime() != null) reservation.updateStartTime(effectiveStart);
        if (request.getEndTime() != null) reservation.updateEndTime(effectiveEnd);

        return ReservationListItemResponse.from(reservation);
    }

    @Transactional
    public void deleteReservations(Long userId, DeleteReservationRequest request) {
        if (request.getReservationIds() == null || request.getReservationIds().isEmpty()) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2010);
        }

        List<Reservation> reservations = reservationRepository.findAllByIdIn(request.getReservationIds());

        if (reservations.size() != request.getReservationIds().size()) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2008);
        }

        boolean hasUnauthorized = reservations.stream()
                .anyMatch(r -> !r.getUser().getId().equals(userId));
        if (hasUnauthorized) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2009);
        }

        reservationRepository.deleteAll(reservations);
    }

    private LocalDate parseDate(String date, ReservationErrorCode missingErrorCode) {
        if (date == null || date.isBlank()) {
            throw new CustomException(missingErrorCode);
        }
        try {
            return LocalDate.parse(date);
        } catch (DateTimeParseException e) {
            throw new CustomException(ReservationErrorCode.RESERVATION_2011);
        }
    }
}
